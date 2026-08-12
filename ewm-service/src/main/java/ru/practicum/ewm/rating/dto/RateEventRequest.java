package ru.practicum.ewm.rating.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.rating.model.RatingValue;

/**
 * Тело запроса на голосование за событие.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateEventRequest {

    @NotNull(message = "Поле 'value' не может быть пустым")
    private RatingValue value;
}
