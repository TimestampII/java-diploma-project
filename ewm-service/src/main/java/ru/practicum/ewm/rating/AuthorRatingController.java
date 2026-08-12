package ru.practicum.ewm.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.rating.dto.UserRatingDto;

/**
 * Public: суммарный рейтинг пользователя как организатора событий
 * (сумма лайков минус дизлайков по всем его событиям). Логирование —
 * в сервисе, где происходит бизнес-логика, а не здесь.
 */
@RestController
@RequestMapping("/users/{userId}/rating")
@RequiredArgsConstructor
public class AuthorRatingController {

    private final RatingService ratingService;

    @GetMapping
    public UserRatingDto getAuthorRating(@PathVariable Long userId) {
        return ratingService.getAuthorRating(userId);
    }
}
