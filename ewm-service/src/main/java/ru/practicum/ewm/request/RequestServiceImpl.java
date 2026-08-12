package ru.practicum.ewm.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userService.getUserOrThrow(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator of the event cannot add a request to participate in it");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in an unpublished event");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }
        if (event.getParticipantLimit() > 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
        }

        // Если лимит заявок = 0 или пре-модерация отключена — подтверждение не требуется.
        RequestStatus status = (event.getParticipantLimit() == 0 || !event.isRequestModeration())
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        Request request = Request.builder()
                .event(event)
                .requester(requester)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        Request saved = requestRepository.save(request);
        log.info("Создана заявка id={} на событие eventId={} от userId={}, статус={}",
                saved.getId(), eventId, userId, status);
        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getUserOrThrow(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }
        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);
        return RequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        getOwnedEventOrThrow(userId, eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        Event event = getOwnedEventOrThrow(userId, eventId);

        List<Request> requests = requestRepository.findAllByIdInAndEventId(updateRequest.getRequestIds(), eventId);
        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Some of the requests were not found for event id=" + eventId);
        }

        boolean anyNotPending = requests.stream().anyMatch(r -> r.getStatus() != RequestStatus.PENDING);
        if (anyNotPending) {
            throw new ValidationException("Request must have status PENDING");
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            rejected.addAll(requests);
        } else if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            long alreadyConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            int limit = event.getParticipantLimit();

            for (Request r : requests) {
                if (limit != 0 && alreadyConfirmed >= limit) {
                    // Лимит исчерпан — все оставшиеся неподтверждённые заявки отклоняются.
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(r);
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(r);
                    alreadyConfirmed++;
                }
            }

            // Если лимит исчерпан этим подтверждением — автоматически отклонить
            // все ОСТАЛЬНЫЕ (не входящие в текущий запрос) PENDING-заявки на это событие.
            if (limit != 0 && alreadyConfirmed >= limit) {
                List<Request> stillPending = requestRepository.findAllByEventId(eventId).stream()
                        .filter(r -> r.getStatus() == RequestStatus.PENDING)
                        .collect(Collectors.toList());
                stillPending.forEach(r -> r.setStatus(RequestStatus.REJECTED));
                rejected.addAll(stillPending);
            }
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(RequestMapper::toDto).collect(Collectors.toList()))
                .rejectedRequests(rejected.stream().map(RequestMapper::toDto).collect(Collectors.toList()))
                .build();
    }

    private Event getOwnedEventOrThrow(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return event;
    }
}
