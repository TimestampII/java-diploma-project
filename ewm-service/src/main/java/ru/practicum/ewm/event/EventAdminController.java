package ru.practicum.ewm.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin: События — поиск по всем событиям без ограничений и модерация
 * (публикация/отклонение), редактирование данных любого события.
 */
@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Admin: поиск событий users={}, states={}, categories={}, from={}-{}",
                users, states, categories, rangeStart, rangeEnd);
        List<EventState> parsedStates = states == null ? Collections.emptyList() : states.stream()
                .map(EventState::valueOf)
                .collect(Collectors.toList());
        List<Long> safeUsers = users == null ? Collections.emptyList() : users;
        List<Long> safeCategories = categories == null ? Collections.emptyList() : categories;
        return eventService.searchEventsAdmin(safeUsers, parsedStates, safeCategories, rangeStart, rangeEnd,
                from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest request) {
        log.info("Admin: изменение события eventId={}, stateAction={}", eventId, request.getStateAction());
        return eventService.updateEventAdmin(eventId, request);
    }
}
