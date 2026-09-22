package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для UserService")
@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepo;

    private Long existingId;

    @BeforeEach
    void setUp() {
        User user = userRepo.save(User.builder()
                .name("Alice").email("alice@example.com").build());
        existingId = user.getId();
    }

    @Test
    @DisplayName("Создание нового пользователя и его сохранение в БД")
    void shouldCreateUserAndPersist() {
        UserDto dto = UserDto.builder()
                .name("Bob").email("bob@example.com").build();

        UserResponse created = userService.createUser(dto);

        assertTrue(userRepo.existsById(created.getId()));
    }

    @Test
    @DisplayName("Создание пользователя с уже существующим email – Conflict")
    void createUserDuplicateEmail() {
        userService.createUser(
                UserDto.builder().name("First").email("dup@example.com").build());

        ConflictException ex = assertThrows(ConflictException.class,
                () -> userService.createUser(
                        UserDto.builder()
                                .name("Second")
                                .email("dup@example.com")
                                .build()));

        assertEquals("Email уже используется", ex.getMessage());
    }

    @Test
    @DisplayName("Обновить пользователя с e‑mail, уже занятой другим пользователем – Conflict")
    void updateUser_emailConflict() {
        UserDto second = UserDto.builder()
                .name("Second")
                .email("second@example.com")
                .build();
        UserResponse r2 = userService.createUser(second);

        UserDto conflictDto = UserDto.builder()
                .id(r2.getId())
                .name("SecondNew")
                .email("alice@example.com")
                .build();

        ConflictException ex = assertThrows(ConflictException.class,
                () -> userService.updateUser(conflictDto));

        assertEquals("Email уже используется", ex.getMessage());
    }


    @Test
    @DisplayName("Удаление существующего пользователя")
    void shouldDeleteUser() {
        UserResponse response = userService.getUserById(existingId);
        userService.deleteUser(response.getId());
        assertFalse(userRepo.existsById(response.getId()));
    }

    @Test
    @DisplayName("Обновить несуществующего пользователя → NotFoundException")
    void updateNonExistingUser() {
        UserDto dto = UserDto.builder()
                .id(999L)
                .name("Nobody")
                .email("nobody@example.com")
                .build();

        assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(dto));
    }
}
