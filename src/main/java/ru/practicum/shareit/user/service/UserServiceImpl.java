package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.exception.model.ConflictException;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserResponse createUser(UserDto request) {
        if (request.getEmail() == null || request.getName() == null) {
            throw new ValidationException("Имя или email не указаны");
        }

        if (userStorage.existsByEmail(request.getEmail())) {
            log.warn("Пользователь с почтой {} уже существует", request.getEmail());
            throw new ConflictException("Email уже используется");
        }

        User user = UserMapper.toEntity(request);

        return UserMapper.toResponse(userStorage.create(user));
    }

    @Override
    public UserResponse updateUser(UserDto request) {
        User user = userStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        UserMapper.mergeFromDto(request, user);

        if (request.getEmail() != null && userStorage.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email уже используется");
        }

        return UserMapper.toResponse(userStorage.update(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return UserMapper.toResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        userStorage.delete(id);
    }
}
