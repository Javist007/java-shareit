package ru.practicum.shareit.user.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor
public class UserMapper {

    public static User toEntity(UserDto request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static void mergeFromDto(UserDto request, User user) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
    }
}
