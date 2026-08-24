package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, User> email = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(this.email.get(email));
    }

    @Override
    public User create(User user) {
        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        email.put(user.getEmail(), user);
        return user;
    }

    @Override
    public User update(User user) {
        User old = users.get(user.getId());
        if (old != null && old.getEmail() != null) {
            email.remove(old.getEmail());
        }
        users.put(user.getId(), user);
        email.put(user.getEmail(), user);
        return user;
    }

    @Override
    public void delete(long id) {
        User user = users.remove(id);
        if (user != null && user.getEmail() != null) {
            email.remove(user.getEmail());
        }
    }
}
