package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Изменение информации о подборке событий. Если поле не указано (null) —
 * изменение этих данных не требуется. Соответствует UpdateCompilationRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    /**
     * Полная замена текущего списка событий подборки, если поле передано.
     */
    private List<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Длина заголовка подборки должна быть от 1 до 50 символов")
    private String title;
}
