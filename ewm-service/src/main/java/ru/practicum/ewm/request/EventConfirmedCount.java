package ru.practicum.ewm.request;

/**
 * Проекция для батч-подсчёта подтверждённых заявок по нескольким событиям
 * одним GROUP BY запросом вместо N+1 запросов.
 */
public interface EventConfirmedCount {
    Long getEventId();

    Long getConfirmedCount();
}
