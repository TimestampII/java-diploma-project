package ru.practicum.ewm.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.rating.dto.UserRatingDto;
import ru.practicum.ewm.rating.model.EventRating;
import ru.practicum.ewm.rating.model.RatingValue;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void rateEvent(Long userId, Long eventId, RatingValue value) {
        log.info("Голосование userId={}, eventId={}, value={}", userId, eventId, value);

        User voter = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator of the event cannot rate their own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot rate an unpublished event");
        }

        // Upsert: если голос уже есть — обновляем значение (лайк -> дизлайк и наоборот),
        // если нет — создаём новый. Один пользователь — один голос на событие.
        EventRating rating = repository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> EventRating.builder()
                        .event(event)
                        .user(voter)
                        .created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                        .build());
        rating.setValue(value);
        repository.save(rating);
        log.info("Голос userId={} за eventId={} сохранён: {}", userId, eventId, value);
    }

    @Override
    @Transactional
    public void removeRating(Long userId, Long eventId) {
        log.info("Отмена голоса userId={}, eventId={}", userId, eventId);

        EventRating rating = repository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Rating from userId=" + userId + " for eventId=" + eventId + " was not found"));
        repository.delete(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRatingDto getAuthorRating(Long userId) {
        log.info("Запрос рейтинга организатора userId={}", userId);

        getUserOrThrow(userId);
        Long rating = repository.sumRatingByAuthorId(userId);
        return UserRatingDto.builder()
                .userId(userId)
                .rating(rating)
                .build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
