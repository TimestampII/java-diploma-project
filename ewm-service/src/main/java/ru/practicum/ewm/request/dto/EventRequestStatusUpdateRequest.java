package ru.practicum.ewm.request.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;

/**
 * Изменение статуса запросов на участие в событии текущего пользователя.
 * Соответствует схеме EventRequestStatusUpdateRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Список requestIds не может быть пустым")
    private List<Long> requestIds;

    @NotNull(message = "Поле 'status' не может быть пустым")
    private RequestStatus status;
}
