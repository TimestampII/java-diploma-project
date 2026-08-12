package ru.practicum.ewm.stats.server.service;

import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    /**
     * Сохранить информацию о том, что был обработан запрос к эндпоинту.
     */
    EndpointHitDto saveHit(EndpointHitDto endpointHitDto);

    /**
     * Получить статистику по посещениям за указанный период,
     * опционально отфильтрованную по списку URI и с учётом уникальности IP.
     */
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
