package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface UserStorage {

    Optional<User> findById(long id);

    boolean existsByEmail(String email);

    User create(User user);

    User update(User user);

    void delete(long id);
}
