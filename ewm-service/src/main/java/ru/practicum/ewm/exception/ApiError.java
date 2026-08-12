package ru.practicum.ewm.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.util.DateTimeConstants;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сведения об ошибке. Состав полей и формат timestamp строго соответствуют
 * схеме ApiError из ewm-main-service-spec.json.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    /**
     * Список стектрейсов или описания ошибок.
     */
    @Builder.Default
    private List<String> errors = List.of();

    /**
     * Сообщение об ошибке.
     */
    private String message;

    /**
     * Общее описание причины ошибки.
     */
    private String reason;

    /**
     * Код статуса HTTP-ответа (например, "BAD_REQUEST").
     */
    private String status;

    /**
     * Дата и время, когда произошла ошибка.
     */
    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime timestamp;
}
