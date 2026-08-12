package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.util.OffsetPageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        User saved = repository.save(UserMapper.toUser(request));
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = OffsetPageRequest.of(from, size);
        Page<User> page = (ids == null || ids.isEmpty())
                ? repository.findAll(pageable)
                : repository.findAllByIdIn(ids, pageable);
        return page.getContent().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserOrThrow(userId);
        repository.delete(user);
    }

    /**
     * Приватный внутренний хелпер, не часть публичного контракта сервиса.
     */
    private User getUserOrThrow(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }
}
