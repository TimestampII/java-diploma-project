package ru.practicum.ewm.user;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

/**
 * Контракт сервиса — только DTO. Другим доменам (события, заявки), которым
 * для построения JPA-связей нужна сама сущность User, а не её представление,
 * следует обращаться напрямую к UserRepository, а не через этот интерфейс —
 * сущность является деталью реализации слоя данных, а не частью контракта сервиса.
 */
public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);
}
