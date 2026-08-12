package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Широта и долгота места проведения события. Соответствует схеме Location.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull(message = "Поле 'lat' не может быть пустым")
    private Float lat;

    @NotNull(message = "Поле 'lon' не может быть пустым")
    private Float lon;
}
