package ru.practicum.ewm.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Данные о запросе к эндпоинту, которые сохраняет сервис статистики.
 * Используется в теле запроса POST /hit и как элемент в GET /stats при необходимости.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

    /**
     * Идентификатор записи — присваивается сервисом статистики, клиент его не передаёт.
     */
    private Long id;

    /**
     * Идентификатор сервиса, для которого записывается информация.
     */
    @NotBlank(message = "Поле 'app' не может быть пустым")
    private String app;

    /**
     * URI, для которого был осуществлён запрос.
     */
    @NotBlank(message = "Поле 'uri' не может быть пустым")
    private String uri;

    /**
     * IP-адрес пользователя, осуществившего запрос.
     */
    @NotBlank(message = "Поле 'ip' не может быть пустым")
    private String ip;

    /**
     * Дата и время, когда был совершён запрос к эндпоинту.
     */
    @NotNull(message = "Поле 'timestamp' не может быть пустым")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
