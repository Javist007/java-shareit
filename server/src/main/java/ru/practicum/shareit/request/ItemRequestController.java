package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @GetMapping("/{requestId}")
    public ItemRequestResponse getById(@PathVariable long requestId,
                                       @RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение запроса: requestId={}, userId={}", requestId, userId);
        return itemRequestService.getById(requestId);
    }

    @PostMapping
    public ItemRequestResponse create(@RequestBody ItemRequestDto request,
                                      @RequestHeader(USER_ID_HEADER) long userId) {
        ItemRequestResponse created = itemRequestService.create(request, userId);
        log.info("Создан запрос вещи: requestId={}, userId={}", created.getId(), userId);
        return created;
    }

    @GetMapping
    public List<ItemRequestResponse> getOwnRequests(@RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение своих запросов: userId={}", userId);
        return itemRequestService.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponse> getAllOtherRequests(@RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение чужих запросов: userId={}", userId);
        return itemRequestService.getAll(userId);
    }
}