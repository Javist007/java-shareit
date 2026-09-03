package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> storage = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Set<Item> findAllByOwnerId(long ownerId) {
        return storage.values().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .collect(Collectors.toSet());
    }

    @Override
    public Item create(Item item) {
        item.setId(counter.getAndIncrement());

        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public void update(Item item) {
        storage.put(item.getId(), item);
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String pattern = text.trim().toLowerCase();
        return storage.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(pattern)
                        || item.getDescription().toLowerCase().contains(pattern))
                .toList();
    }
}
