package ru.practicum.ewm.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Клиент для взаимодействия основного сервиса с сервисом статистики.
 * Оборачивает HTTP-запросы к эндпоинтам POST /hit и GET /stats.
 */
@Slf4j
@Component
public class StatsClient {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplateBuilder builder,
                        @Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        this.serverUrl = serverUrl;
        this.restTemplate = builder.rootUri(serverUrl).build();
    }

    /**
     * Отправляет в сервис статистики информацию об обращении к эндпоинту.
     */
    public void saveHit(EndpointHitDto endpointHitDto) {
        try {
            restTemplate.postForEntity("/hit", endpointHitDto, Void.class);
        } catch (Exception e) {
            // Сбой сервиса статистики не должен ломать основной пользовательский сценарий.
            log.warn("Не удалось отправить данные в сервис статистики: {}", e.getMessage());
        }
    }

    /**
     * Запрашивает статистику посещений за период, опционально по конкретным URI.
     */
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(DATE_FORMATTER))
                .queryParam("end", end.format(DATE_FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris.toArray());
        }

        String uri = uriBuilder.encode().toUriString();

        ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(uri, ViewStatsDto[].class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return List.of();
        }
        return List.of(response.getBody());
    }
}
