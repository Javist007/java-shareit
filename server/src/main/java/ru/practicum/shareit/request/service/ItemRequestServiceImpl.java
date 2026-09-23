package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ItemRequestResponse getById(long requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос предмета не найден"));

        log.debug("Найден запрос: {}", request);
        return ItemRequestMapper.toResponse(request, request.getItems());
    }

    @Override
    @Transactional
    public ItemRequestResponse create(ItemRequestDto request, long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest saved = itemRequestRepository.save(
                ItemRequestMapper.toEntity(request, requestor));

        log.debug("Запрос предмета с id={} сохранён", saved.getId());
        return ItemRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponse> getOwn(long userId) {
        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        log.debug("Найдено {} собственных заявок", requests.size());
        return requests.stream()
                .map(ItemRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponse> getAll(long userId) {
        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);

        log.debug("Найдено {} чужих заявок", requests.size());
        return requests.stream()
                .map(ItemRequestMapper::toResponse)
                .toList();
    }
}