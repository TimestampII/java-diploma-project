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
 * Краткая информация о событии. Соответствует схеме EventShortDto из спецификации.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {

    private Long id;

    private String title;

    private String annotation;

    private CategoryDto category;

    private UserShortDto initiator;

    private boolean paid;

    @JsonFormat(pattern = DateTimeConstants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private Long confirmedRequests;

    private Long views;
}
