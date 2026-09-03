package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.service.UserService;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Valid UserDto userDto) {
        UserResponse created = userService.createUser(userDto);
        log.info("Создан пользователь: id={}", created.getId());
        return created;
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable long id, @RequestBody UserDto userDto) {
        userDto.setId(id);
        UserResponse updated = userService.updateUser(userDto);
        log.info("Обновлен пользователь: id={}", updated.getId());
        return updated;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable long id) {
        log.debug("Получение пользователя: id={}", id);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        userService.deleteUser(id);
        log.info("Удален пользователь: id={}", id);
    }
}
