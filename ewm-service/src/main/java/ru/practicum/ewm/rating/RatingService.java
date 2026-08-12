package ru.practicum.ewm.rating;

import ru.practicum.ewm.rating.dto.UserRatingDto;
import ru.practicum.ewm.rating.model.RatingValue;

public interface RatingService {

    void rateEvent(Long userId, Long eventId, RatingValue value);

    void removeRating(Long userId, Long eventId);

    UserRatingDto getAuthorRating(Long userId);
}
