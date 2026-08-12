package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

/**
 * Подборка событий. Соответствует схеме CompilationDto из спецификации.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    private Long id;

    private List<EventShortDto> events;

    private boolean pinned;

    private String title;
}
