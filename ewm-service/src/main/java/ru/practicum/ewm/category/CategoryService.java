package ru.practicum.ewm.category;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto request);

    CategoryDto updateCategory(Long catId, CategoryDto request);

    void deleteCategory(Long catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);

    /**
     * Возвращает сущность категории или бросает NotFoundException.
     * Используется доменом событий для установления связи Event -> Category.
     */
    Category getCategoryOrThrow(Long catId);
}
