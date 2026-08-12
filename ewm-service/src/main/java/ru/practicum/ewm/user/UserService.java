package ru.practicum.ewm.user;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);

    /**
     * Возвращает сущность пользователя или бросает NotFoundException.
     * Используется другими доменами (события, заявки), которым нужна не DTO,
     * а сама сущность для построения связей.
     */
    ru.practicum.ewm.user.User getUserOrThrow(Long userId);
}
