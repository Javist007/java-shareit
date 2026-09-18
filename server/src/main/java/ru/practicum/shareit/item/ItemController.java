package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.comment.CommentCreateDto;
import ru.practicum.shareit.item.dto.comment.CommentResponse;
import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;
import ru.practicum.shareit.item.service.comment.CommentService;
import ru.practicum.shareit.item.service.item.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ItemResponse create(@RequestBody ItemDto request,
                               @RequestHeader(USER_ID_HEADER) long userId) {
        ItemResponse created = itemService.addItem(request, userId);
        log.info("Создана вещь: itemId={} userId={}", created.getId(), userId);
        return created;
    }

    @PatchMapping("/{itemId}")
    public ItemResponse update(@PathVariable long itemId,
                               @RequestBody ItemDto itemDto,
                               @RequestHeader(USER_ID_HEADER) long userId) {
        ItemResponse updated = itemService.updateItem(itemId, itemDto, userId);
        log.info("Обновление вещи: itemId={} userId={}", updated.getId(), userId);
        return updated;
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItem(@PathVariable long itemId,
                                @RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение вещи: id={}, userId={}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemResponse> getAll(@RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение вещей пользователя: userId={}", userId);
        return itemService.findAllOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<ItemResponse> search(@RequestParam String text) {
        log.info("Поиск вещей: text='{}'", text);
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse addComment(@PathVariable long itemId,
                                      @RequestBody CommentCreateDto request,
                                      @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Добавление комментария: itemId={}, userId={}", itemId, userId);
        return commentService.addComment(itemId, request, userId);
    }
}
