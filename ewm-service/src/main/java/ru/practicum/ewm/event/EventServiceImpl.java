package ru.practicum.ewm.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.UserStateAction;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserService;
import ru.practicum.ewm.util.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String APP_NAME = "ewm-main-service";
    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private static final int MIN_HOURS_BETWEEN_PUBLISH_AND_EVENT = 1;
    // Начало отсчёта при запросе статистики по всем событиям - достаточно старая дата,
    // чтобы гарантированно охватить всю историю просмотров.
    private static final LocalDateTime STATS_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0);

    private final EventRepository repository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsClient statsClient;
    private final ru.practicum.ewm.request.RequestRepository requestRepository;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto request) {
        User initiator = userService.getUserOrThrow(userId);
        Category category = categoryService.getCategoryOrThrow(request.getCategory());
        validateEventDateForCreateOrUpdate(request.getEventDate());

        Event event = EventMapper.toEvent(request, category, initiator);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = repository.save(event);
        log.info("Создано событие id={} инициатором userId={}", saved.getId(), userId);
        return EventMapper.toEventFullDto(saved, 0L, 0L);
        // confirmedRequests=0 здесь корректно и без запроса к RequestRepository:
        // у только что созданного события физически не может быть заявок.
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userService.getUserOrThrow(userId);
        Pageable pageable = OffsetPageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        Page<Event> page = repository.findByInitiatorId(userId, pageable);
        Map<Long, Long> views = getViewsForEvents(page.getContent());
        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(page.getContent());
        return page.getContent().stream()
                .map(e -> EventMapper.toEventShortDto(e, confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = getOwnedEventOrThrow(userId, eventId);
        long views = getViewsForEvents(List.of(event)).getOrDefault(event.getId(), 0L);
        long confirmed = getConfirmedRequestsForEvents(List.of(event)).getOrDefault(event.getId(), 0L);
        return EventMapper.toEventFullDto(event, confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getOwnedEventOrThrow(userId, eventId);

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getEventDate() != null) {
            validateEventDateForCreateOrUpdate(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }

        applyCommonFields(event, request.getAnnotation(), request.getCategory(), request.getDescription(),
                request.getLocation(), request.getPaid(), request.getParticipantLimit(),
                request.getRequestModeration(), request.getTitle());

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (request.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = repository.save(event);
        long views = getViewsForEvents(List.of(saved)).getOrDefault(saved.getId(), 0L);
        long confirmed = getConfirmedRequestsForEvents(List.of(saved)).getOrDefault(saved.getId(), 0L);
        return EventMapper.toEventFullDto(saved, confirmed, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 int from, int size) {
        Specification<Event> spec = EventSpecifications.adminSearch(users, states, categories, rangeStart, rangeEnd);
        Pageable pageable = OffsetPageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        Page<Event> page = repository.findAll(spec, pageable);
        Map<Long, Long> views = getViewsForEvents(page.getContent());
        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(page.getContent());
        return page.getContent().stream()
                .map(e -> EventMapper.toEventFullDto(e, confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        applyCommonFields(event, request.getAnnotation(), request.getCategory(), request.getDescription(),
                request.getLocation(), request.getPaid(), request.getParticipantLimit(),
                request.getRequestModeration(), request.getTitle());

        if (request.getStateAction() != null) {
            applyAdminStateAction(event, request.getStateAction());
        }

        Event saved = repository.save(event);
        long views = getViewsForEvents(List.of(saved)).getOrDefault(saved.getId(), 0L);
        long confirmed = getConfirmedRequestsForEvents(List.of(saved)).getOrDefault(saved.getId(), 0L);
        return EventMapper.toEventFullDto(saved, confirmed, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> searchEventsPublic(String text, List<Long> categories, Boolean paid,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                   boolean onlyAvailable, EventSort sort, int from, int size,
                                                   String clientIp, String requestUri) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ConflictException("Дата начала диапазона не может быть позже даты окончания");
        }

        recordHit(clientIp, requestUri);

        Specification<Event> spec = EventSpecifications.publicSearch(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable);
        Pageable pageable = OffsetPageRequest.of(from, size);
        Page<Event> page = repository.findAll(spec, pageable);

        Map<Long, Long> views = getViewsForEvents(page.getContent());
        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(page.getContent());

        List<EventShortDto> result = page.getContent().stream()
                .map(e -> EventMapper.toEventShortDto(e, confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());

        if (sort == EventSort.VIEWS) {
            result.sort(Comparator.comparing(EventShortDto::getViews, Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublishedEvent(Long eventId, String clientIp, String requestUri) {
        Event event = repository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        recordHit(clientIp, requestUri);

        long views = getViewsForEvents(List.of(event)).getOrDefault(event.getId(), 0L);
        long confirmed = getConfirmedRequestsForEvents(List.of(event)).getOrDefault(event.getId(), 0L);
        return EventMapper.toEventFullDto(event, confirmed, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> mapToShortDtos(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> views = getViewsForEvents(events);
        Map<Long, Long> confirmed = getConfirmedRequestsForEvents(events);
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e, confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------------
    // Вспомогательные методы
    // ---------------------------------------------------------------------

    private Event getOwnedEventOrThrow(Long userId, Long eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return event;
    }

    private void validateEventDateForCreateOrUpdate(LocalDateTime eventDate) {
        LocalDateTime minAllowed = LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT);
        if (eventDate.isBefore(minAllowed)) {
            throw new ConflictException(
                    "Field: eventDate. Error: дата и время события должны быть не ранее чем через "
                            + MIN_HOURS_BEFORE_EVENT + " часа от текущего момента. Value: " + eventDate);
        }
    }

    private void applyAdminStateAction(Event event, AdminStateAction action) {
        if (action == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException(
                        "Cannot publish the event because it's not in the right state: " + event.getState());
            }
            LocalDateTime publishTime = LocalDateTime.now();
            LocalDateTime minEventDate = publishTime.plusHours(MIN_HOURS_BETWEEN_PUBLISH_AND_EVENT);
            if (event.getEventDate().isBefore(minEventDate)) {
                throw new ConflictException(
                        "Field: eventDate. Error: дата начала события должна быть не ранее чем через "
                                + MIN_HOURS_BETWEEN_PUBLISH_AND_EVENT + " час от даты публикации. Value: "
                                + event.getEventDate());
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(publishTime);
        } else if (action == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException(
                        "Cannot reject the event because it's not in the right state: " + event.getState());
            }
            event.setState(EventState.CANCELED);
        }
    }

    private void applyCommonFields(Event event, String annotation, Long categoryId, String description,
                                    LocationDto location, Boolean paid, Integer participantLimit,
                                    Boolean requestModeration, String title) {
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (categoryId != null) {
            event.setCategory(categoryService.getCategoryOrThrow(categoryId));
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (location != null) {
            event.setLocation(Location.builder().lat(location.getLat()).lon(location.getLon()).build());
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null) {
            event.setTitle(title);
        }
    }

    private void recordHit(String clientIp, String requestUri) {
        statsClient.saveHit(EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(requestUri)
                .ip(clientIp)
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * Запрашивает у сервиса статистики уникальные просмотры для списка событий одним запросом
     * (а не по одному на событие — иначе это была бы лишняя нагрузка на БД/сеть).
     */
    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> uriToEventId = events.stream()
                .collect(Collectors.toMap(e -> "/events/" + e.getId(), Event::getId));

        List<ViewStatsDto> stats = statsClient.getStats(
                STATS_EPOCH, LocalDateTime.now(), List.copyOf(uriToEventId.keySet()), true);

        return stats.stream()
                .filter(s -> uriToEventId.containsKey(s.getUri()))
                .collect(Collectors.toMap(
                        s -> uriToEventId.get(s.getUri()),
                        ViewStatsDto::getHits,
                        Long::sum));
    }

    /**
     * Считает подтверждённые заявки для списка событий одним запросом к БД
     * (group by event_id), а не по одному запросу на событие.
     */
    private Map<Long, Long> getConfirmedRequestsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        return requestRepository.countConfirmedByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        ru.practicum.ewm.request.EventConfirmedCount::getEventId,
                        ru.practicum.ewm.request.EventConfirmedCount::getConfirmedCount));
    }
}
