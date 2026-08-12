package ru.practicum.ewm.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Управление заявками на СВОЁ событие (по спецификации относится к тегу
 * "Private: События", т.к. путь вложен в /users/{userId}/events/{eventId}, но
 * логически принадлежит домену Requests — здесь и реализовано).
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class EventRequestManagementController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос заявок на событие userId={}, eventId={}", userId, eventId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId, @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса заявок userId={}, eventId={}, status={}",
                userId, eventId, request.getStatus());
        return requestService.updateRequestStatus(userId, eventId, request);
    }
}
