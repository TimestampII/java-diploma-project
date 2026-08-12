package ru.practicum.ewm.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.util.OffsetPageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {
        Set<Event> events = resolveEvents(request.getEvents());

        Compilation compilation = Compilation.builder()
                .title(request.getTitle())
                .pinned(Boolean.TRUE.equals(request.getPinned()))
                .events(events)
                .build();

        Compilation saved = repository.save(compilation);
        log.info("Создана подборка id={}, title={}, events={}", saved.getId(), saved.getTitle(), events.size());
        return toDtoWithEvents(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = getCompilationOrThrow(compId);
        repository.delete(compilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationOrThrow(compId);

        Optional.ofNullable(request.getTitle()).ifPresent(compilation::setTitle);
        Optional.ofNullable(request.getPinned()).ifPresent(compilation::setPinned);
        Optional.ofNullable(request.getEvents())
                .ifPresent(eventIds -> compilation.setEvents(resolveEvents(eventIds)));

        Compilation saved = repository.save(compilation);
        return toDtoWithEvents(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = OffsetPageRequest.of(from, size);
        Page<Compilation> page = pinned == null
                ? repository.findAll(pageable)
                : repository.findAllByPinned(pinned, pageable);
        return page.getContent().stream()
                .map(this::toDtoWithEvents)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        return toDtoWithEvents(getCompilationOrThrow(compId));
    }

    private Compilation getCompilationOrThrow(Long compId) {
        return repository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private Set<Event> resolveEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Event> found = eventRepository.findAllById(eventIds);
        if (found.size() != new HashSet<>(eventIds).size()) {
            List<Long> foundIds = found.stream().map(Event::getId).collect(Collectors.toList());
            List<Long> missing = eventIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new NotFoundException("Events not found: " + missing);
        }
        return new LinkedHashSet<>(found);
    }

    private CompilationDto toDtoWithEvents(Compilation compilation) {
        List<Event> events = new ArrayList<>(compilation.getEvents());
        List<EventShortDto> eventDtos = events.isEmpty()
                ? Collections.emptyList()
                : eventService.mapToShortDtos(events);
        return CompilationMapper.toDto(compilation, eventDtos);
    }
}
