package ru.practicum.ewm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Пользователь (краткая информация). Используется, например, как поле
 * initiator в EventFullDto/EventShortDto. Соответствует схеме UserShortDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    private Long id;

    private String name;
}
