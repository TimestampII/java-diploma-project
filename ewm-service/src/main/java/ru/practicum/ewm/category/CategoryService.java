package ru.practicum.ewm.category;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

/**
 * Контракт сервиса — только DTO. Домену событий, которому для установления
 * связи Event -> Category нужна сама сущность, а не её представление,
 * следует обращаться напрямую к CategoryRepository, а не через этот интерфейс.
 */
public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto request);

    CategoryDto updateCategory(Long catId, CategoryDto request);

    void deleteCategory(Long catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);
}
