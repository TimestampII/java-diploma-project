package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.util.DateTimeConstants;

import java.time.LocalDateTime;

/**
 * Новое событие. Соответствует схеме NewEventDto из спецификации.
 * Бизнес-правило "eventDate не раньше чем через 2 часа от текущего момента"
 * проверяется в сервисе (409), а не здесь (400) — так задано в спецификации.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Поле 'annotation' не может быть пустым")
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "Поле 'category' не может быть пустым")
    private Long category;

    @NotBlank(message = "Поле 'description' не может быть пустым")
    @Size(min = 20, max = 7000, message = "Длина описания должна быть от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Поле 'eventDate' не может быть пустым")
    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @NotNull(message = "Поле 'location' не может быть пустым")
    @Valid
    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Поле 'title' не может быть пустым")
    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от 3 до 120 символов")
    private String title;
}
