package ru.practicum.ewm.rating;

/**
 * Проекция для батч-подсчёта рейтинга по нескольким событиям одним
 * GROUP BY запросом вместо N+1.
 */
public interface EventRatingSum {
    Long getEventId();

    Long getRating();
}
