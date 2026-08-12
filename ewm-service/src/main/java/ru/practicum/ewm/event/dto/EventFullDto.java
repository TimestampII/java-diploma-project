package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.util.DateTimeConstants;

import java.time.LocalDateTime;

/**
 * Полная информация о событии. Соответствует схеме EventFullDto из спецификации.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private Long id;

    private String title;

    private String annotation;

    private String description;

    private CategoryDto category;

    private UserShortDto initiator;

    private LocationDto location;

    private boolean paid;

    private int participantLimit;

    private boolean requestModeration;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;

    private String state;

    private Long confirmedRequests;

    private Long views;

    /**
     * Чистый рейтинг события (лайки минус дизлайки). Поле добавлено
     * в рамках дополнительной функциональности rating_events и не
     * входит в базовую спецификацию основного сервиса.
     */
    private Long rating;
}
