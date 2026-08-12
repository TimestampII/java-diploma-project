package ru.practicum.ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Суммарный рейтинг автора — сумма (лайки - дизлайки) по всем его событиям.
 * Отдельный DTO, а не поле в UserDto, чтобы не расширять DTO, зафиксированный
 * спецификацией основного сервиса.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingDto {

    private Long userId;

    private Long rating;
}
