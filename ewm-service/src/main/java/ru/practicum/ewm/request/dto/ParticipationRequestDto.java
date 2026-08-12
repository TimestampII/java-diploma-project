package ru.practicum.ewm.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Заявка на участие в событии. Соответствует схеме ParticipationRequestDto
 * из спецификации. Обратите внимание: в отличие от остальных DTO проекта,
 * здесь поле created сериализуется в стандартном ISO-формате Jackson
 * (например "2022-09-06T21:10:05.432"), а не в "yyyy-MM-dd HH:mm:ss" —
 * именно так это поле описано в спецификации основного сервиса.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private Long id;

    private Long event;

    private Long requester;

    private String status;

    private LocalDateTime created;
}
