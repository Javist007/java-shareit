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
import ru.practicum.shareit.user.storage.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(UserDto request) {
        if (request.getEmail() == null || request.getName() == null) {
            throw new ValidationException("Имя или email не указаны");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Пользователь с почтой {} уже существует", request.getEmail());
            throw new ConflictException("Email уже используется");
        }

        User user = UserMapper.toEntity(request);

        log.debug("Создание пользователя: email={}, name={}", request.getEmail(), request.getName());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(UserDto request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        String oldEmail = user.getEmail();

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (!oldEmail.equals(user.getEmail()) &&
            userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            throw new ConflictException("Email уже используется");
        }

        log.info("Пользователь {} обновлён: new data={}", request.getId(), user);
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return UserMapper.toResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }
}
