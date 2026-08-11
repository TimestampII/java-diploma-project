package ru.practicum.ewm.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Формат даты и времени для параметров start/end настроен глобально
 * через spring.mvc.format.date-time в application.yml, поэтому
 * @DateTimeFormat на каждом параметре здесь больше не нужен.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * Сохранить информацию о том, что к эндпоинту был отправлен запрос.
     */
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto saveHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Сохранение обращения: app={}, uri={}, ip={}", endpointHitDto.getApp(),
                endpointHitDto.getUri(), endpointHitDto.getIp());
        return statsService.saveHit(endpointHitDto);
    }

    /**
     * Получить статистику по посещениям.
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        List<String> safeUris = uris == null ? Collections.emptyList() : uris;
        return statsService.getStats(start, end, safeUris, unique);
    }
}