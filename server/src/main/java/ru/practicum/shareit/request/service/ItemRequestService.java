package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponse getById(long requestId);

    ItemRequestResponse create(ItemRequestDto request, long userId);

    List<ItemRequestResponse> getOwn(long userId);

    List<ItemRequestResponse> getAll(long userId);
}