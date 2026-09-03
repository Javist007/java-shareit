package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.model.ForbiddenException;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemResponse addItem(ItemDto request, long ownerId) {
        User owner = userStorage.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toEntity(request);
        item.setOwner(owner);

        return ItemMapper.toResponse(itemStorage.create(item));
    }

    @Override
    public ItemResponse updateItem(long itemId, ItemDto request, long ownerId) {
        Item existing = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (existing.getOwner().getId() != ownerId) {
            throw new ForbiddenException("Только владелец может обновить элемент");
        }

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            existing.setAvailable(request.getAvailable());
        }

        itemStorage.update(existing);

        return ItemMapper.toResponse(existing);
    }

    @Override
    public ItemResponse getItemById(long id) {
        Item item = itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        return ItemMapper.toResponse(item);
    }

    @Override
    public List<ItemResponse> findAllByOwner(long ownerId) {
        if (userStorage.findById(ownerId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        return itemStorage.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toResponse)
                .toList();
    }

    @Override
    public List<ItemResponse> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemStorage.search(text).stream()
                .map(ItemMapper::toResponse)
                .toList();
    }
}
