package ru.practicum.ewm.compilation;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

public class CompilationMapper {

    private CompilationMapper() {
    }

    public static CompilationDto toDto(Compilation compilation, List<EventShortDto> eventDtos) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventDtos)
                .build();
    }
}
