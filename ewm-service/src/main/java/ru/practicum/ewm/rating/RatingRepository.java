package ru.practicum.ewm.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.rating.model.EventRating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<EventRating, Long> {

    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Считает чистый рейтинг (лайки минус дизлайки) сразу для списка событий
     * одним запросом с GROUP BY — используется при отображении списков событий,
     * чтобы не делать по запросу на каждое событие.
     */
    @Query("select r.event.id as eventId, "
            + "sum(case when r.value = ru.practicum.ewm.rating.model.RatingValue.LIKE then 1 "
            + "         when r.value = ru.practicum.ewm.rating.model.RatingValue.DISLIKE then -1 "
            + "         else 0 end) as rating "
            + "from EventRating r "
            + "where r.event.id in :eventIds "
            + "group by r.event.id")
    List<EventRatingSum> sumRatingsByEventIds(@Param("eventIds") List<Long> eventIds);

    /**
     * Суммарный рейтинг автора — сумма чистого рейтинга по всем его событиям.
     */
    @Query("select coalesce(sum(case when r.value = ru.practicum.ewm.rating.model.RatingValue.LIKE then 1 "
            + "                     when r.value = ru.practicum.ewm.rating.model.RatingValue.DISLIKE then -1 "
            + "                     else 0 end), 0) "
            + "from EventRating r "
            + "where r.event.initiator.id = :userId")
    Long sumRatingByAuthorId(@Param("userId") Long userId);
}
