package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Категория. Соответствует схеме CategoryDto из спецификации.
 * Используется как для ответов, так и как тело запроса PATCH /admin/categories/{catId} —
 * поэтому на name есть валидация.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Поле 'name' не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории должна быть от 1 до 50 символов")
    private String name;
}
