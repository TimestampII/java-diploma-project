package ru.practicum.ewm.rating;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.rating.dto.RateEventRequest;

/**
 * Private: голосование за опубликованные события (лайк/дизлайк).
 * Один пользователь — один голос на событие; повторный запрос с другим
 * значением просто меняет голос.
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void rateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                           @Valid @RequestBody RateEventRequest request) {
        log.info("Голосование userId={}, eventId={}, value={}", userId, eventId, request.getValue());
        ratingService.rateEvent(userId, eventId, request.getValue());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRating(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Отмена голоса userId={}, eventId={}", userId, eventId);
        ratingService.removeRating(userId, eventId);
    }
}
