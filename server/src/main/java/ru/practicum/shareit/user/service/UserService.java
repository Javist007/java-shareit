package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserDto dto);

    UserResponse updateUser(UserDto dto);

    UserResponse getUserById(Long id);

    void deleteUser(Long id);
}
