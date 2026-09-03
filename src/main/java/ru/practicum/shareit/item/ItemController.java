package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponse;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemResponse create(@Valid @RequestBody ItemDto request,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        ItemResponse created = itemService.addItem(request, userId);
        log.info("Создана вещь: itemId={} userId={}", created.getId(), userId);
        return created;
    }

    @PatchMapping("/{itemId}")
    public ItemResponse update(@PathVariable long itemId,
                               @RequestBody ItemDto itemDto,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        ItemResponse updated = itemService.updateItem(itemId, itemDto, userId);
        log.info("Обновление вещи: itemId={} userId={}", updated.getId(), userId);
        return updated;
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItem(@PathVariable long itemId) {
        log.debug("Получение вещи: id={}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemResponse> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.debug("Получение вещей пользователя: userId={}", userId);
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemResponse> search(@RequestParam String text) {
        log.info("Поиск вещей: text='{}'", text);
        return itemService.search(text);
    }

}
