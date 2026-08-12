package ru.practicum.ewm.event;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    // ---- Private: события текущего пользователя ----

    EventFullDto createEvent(Long userId, NewEventDto request);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    // ---- Admin ----

    List<EventFullDto> searchEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    // ---- Public ----

    List<EventShortDto> searchEventsPublic(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            boolean onlyAvailable, EventSort sort, int from, int size,
                                            String clientIp, String requestUri);

    EventFullDto getPublishedEvent(Long eventId, String clientIp, String requestUri);
}
