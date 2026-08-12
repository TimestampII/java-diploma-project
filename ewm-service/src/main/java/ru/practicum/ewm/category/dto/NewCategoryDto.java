package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Данные для добавления новой категории. Соответствует схеме NewCategoryDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @NotBlank(message = "Поле 'name' не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории должна быть от 1 до 50 символов")
    private String name;
}
