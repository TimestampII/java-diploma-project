package ru.practicum.ewm.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.util.OffsetPageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    // TODO(этап "Events"): внедрить EventRepository и перед удалением проверять
    // repository.existsByCategoryId(catId) -> если true, бросать
    // new ConflictException("The category is not empty") согласно спецификации
    // (409 на DELETE /admin/categories/{catId}, когда с категорией связаны события).

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto request) {
        Category saved = repository.save(CategoryMapper.toCategory(request));
        return CategoryMapper.toCategoryDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto request) {
        Category category = getCategoryOrThrow(catId);
        category.setName(request.getName());
        return CategoryMapper.toCategoryDto(repository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = getCategoryOrThrow(catId);
        repository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = OffsetPageRequest.of(from, size);
        Page<Category> page = repository.findAll(pageable);
        return page.getContent().stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long catId) {
        return CategoryMapper.toCategoryDto(getCategoryOrThrow(catId));
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryOrThrow(Long catId) {
        return repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}
