package ru.practicum.shareit.item.service.item;

import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;

import java.util.List;

public interface ItemService {

    ItemResponse addItem(ItemDto request, long ownerId);

    ItemResponse updateItem(long itemId, ItemDto request, long ownerId);

    ItemResponse getItemById(long id, long userId);

    List<ItemResponse> findAllOwnerItems(long ownerId);

    List<ItemResponse> search(String text);
}
