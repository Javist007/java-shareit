package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemStorage {

    Optional<Item> findById(long id);

    Set<Item> findAllByOwnerId(long ownerId);

    Item create(Item item);

    void update(Item item);

    List<Item> search(String text);
}
