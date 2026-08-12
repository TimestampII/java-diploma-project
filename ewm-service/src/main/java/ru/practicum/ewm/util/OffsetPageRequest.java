package ru.practicum.ewm.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Спецификация API задаёт пагинацию через смещение ({@code from}) и размер страницы
 * ({@code size}), а не через номер страницы. Стандартный {@link org.springframework.data.domain.PageRequest}
 * этого не умеет (page * size должно совпадать со смещением), поэтому реализуем
 * Pageable вручную поверх произвольного offset.
 */
public class OffsetPageRequest implements Pageable {

    private final int offset;
    private final int limit;
    private final Sort sort;

    private OffsetPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Смещение 'from' не может быть отрицательным");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Размер страницы 'size' должен быть положительным");
        }
        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
    }

    public static OffsetPageRequest of(int from, int size) {
        return new OffsetPageRequest(from, size, Sort.unsorted());
    }

    public static OffsetPageRequest of(int from, int size, Sort sort) {
        return new OffsetPageRequest(from, size, sort);
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + limit, limit, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return offset - limit < 0 ? this : new OffsetPageRequest(offset - limit, limit, sort);
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, limit, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetPageRequest(pageNumber * limit, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
