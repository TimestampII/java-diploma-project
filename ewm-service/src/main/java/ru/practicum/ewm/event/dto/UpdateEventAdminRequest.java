package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.util.DateTimeConstants;

import java.time.LocalDateTime;

/**
 * Данные для изменения информации о событии администратором.
 * Если поле в запросе не указано (равно null) — значит изменение этих данных
 * не требуется. Соответствует схеме UpdateEventAdminRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть от 20 до 2000 символов")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Длина описания должна быть от 20 до 7000 символов")
    private String description;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @Valid
    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private AdminStateAction stateAction;

    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от 3 до 120 символов")
    private String title;
}
