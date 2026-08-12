package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Данные для добавления новой подборки. Соответствует схеме NewCompilationDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Поле 'title' не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина заголовка подборки должна быть от 1 до 50 символов")
    private String title;
}
