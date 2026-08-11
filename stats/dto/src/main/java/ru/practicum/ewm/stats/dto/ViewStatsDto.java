package ru.practicum.ewm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Статистика по количеству просмотров (обращений) для конкретного
 * сервиса и эндпоинта. Возвращается в ответ на GET /stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {

    /**
     * Идентификатор сервиса, для которого собиралась статистика.
     */
    private String app;

    /**
     * URI сервиса, для которого собиралась статистика.
     */
    private String uri;

    /**
     * Количество просмотров. Считается число уникальных IP,
     * либо общее число запросов — в зависимости от параметра unique.
     */
    private Long hits;
}
