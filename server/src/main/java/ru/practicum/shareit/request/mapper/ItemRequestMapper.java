package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequest toEntity(ItemRequestDto request, User requestor) {
        return ItemRequest.builder()
                .description(request.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponse toResponse(ItemRequest request, List<Item> items) {
        return ItemRequestResponse.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(ItemRequestMapper::toItemDescription)
                        .toList())
                .build();
    }

    public static ItemRequestResponse toResponse(ItemRequest request) {
        return ItemRequestResponse.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(List.of())
                .build();
    }

    private static ItemRequestResponse.ItemDescription toItemDescription(Item item) {
        return ItemRequestResponse.ItemDescription.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }
}