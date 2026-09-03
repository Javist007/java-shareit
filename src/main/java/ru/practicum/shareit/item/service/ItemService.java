package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponse;

import java.util.List;

public interface ItemService {

    ItemResponse addItem(ItemDto request, long ownerId);

    ItemResponse updateItem(long itemId, ItemDto request, long ownerId);

    ItemResponse getItemById(long id);

    List<ItemResponse> findAllByOwner(long ownerId);

    List<ItemResponse> search(String text);
}
