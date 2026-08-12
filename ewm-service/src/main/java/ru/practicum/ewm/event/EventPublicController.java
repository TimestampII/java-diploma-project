package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public: События — доступны без авторизации, поиск с фильтрацией и просмотр
 * подробностей опубликованного события. Каждый запрос фиксируется в сервисе
 * статистики (IP и путь берутся из HttpServletRequest). Контроллер только
 * принимает и передаёт параметры дальше — null-safety для списков живёт в сервисе.
 */
@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> searchEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            HttpServletRequest httpRequest) {
        log.info("Public: поиск событий text={}, categories={}, paid={}", text, categories, paid);
        return eventService.searchEventsPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, httpRequest.getRemoteAddr(), httpRequest.getRequestURI());
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest httpRequest) {
        log.info("Public: запрос события id={}", id);
        return eventService.getPublishedEvent(id, httpRequest.getRemoteAddr(), httpRequest.getRequestURI());
    }
}
