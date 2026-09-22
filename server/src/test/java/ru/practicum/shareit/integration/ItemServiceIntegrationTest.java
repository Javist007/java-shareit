package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;
import ru.practicum.shareit.item.service.item.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.model.ForbiddenException;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для ItemService")
@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ItemRequestRepository requestRepo;

    private User owner;
    private User outsider;
    private ItemRequest request;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        owner = userRepo.save(User.builder()
                .name("Owner").email("owner@example.com").build());
        outsider = userRepo.save(User.builder()
                .name("Outsider").email("out@example.com").build());

        request = requestRepo.save(ItemRequest.builder()
                .description("Need a drill")
                .requestor(owner)
                .created(now.minusDays(1))
                .build());
    }

    @Test
    @DisplayName("Создание предмета без запроса")
    void shouldCreateItemWithoutRequest() {
        ItemDto dto = ItemDto.builder()
                .name("Drill").description("Electric drill")
                .available(Boolean.TRUE)
                .build();

        ItemResponse created =
                itemService.addItem(dto, owner.getId());

        assertNotNull(created);
        assertEquals("Drill", created.getName());
        assertTrue(created.getAvailable());
        assertNull(created.getRequestId());
    }

    @Test
    @DisplayName("Создание предмета с запросом")
    void shouldCreateItemWithRequest() {
        ItemDto dto = ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(Boolean.TRUE)
                .requestId(request.getId())
                .build();


        ItemResponse created =
                itemService.addItem(dto, owner.getId());

        assertEquals(request.getId(), created.getRequestId());
    }

    @Test
    @DisplayName("Обновление предмета владельцем")
    void shouldUpdateItemByOwner() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(Boolean.TRUE)
                .build();

        ItemResponse created =
                itemService.addItem(dto, owner.getId());

        ItemDto upd = ItemDto.builder()
                .name("Power Drill")
                .available(Boolean.FALSE)
                .build();

        ItemResponse updated =
                itemService.updateItem(created.getId(), upd, owner.getId());

        assertEquals("Power Drill", updated.getName());
        assertFalse(updated.getAvailable());
    }

    @Test
    @DisplayName("Невозможно обновить предмет не‑владельцем")
    void shouldNotUpdateItemByNonOwner() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(Boolean.TRUE)
                .build();

        ItemResponse created =
                itemService.addItem(dto, owner.getId());

        ItemDto upd = ItemDto.builder()
                .name("Power Drill")
                .build();

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(created.getId(), upd, outsider.getId()));
    }

    @Test
    @DisplayName("Получить предмет по id: владелец и любой пользователь могут читать")
    void shouldReadItemByAnyUser() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        ItemResponse created =
                itemService.addItem(dto, owner.getId());

        ItemResponse ownerView =
                itemService.getItemById(created.getId(), owner.getId());
        assertEquals(created.getId(), ownerView.getId());

        ItemResponse outsiderView =
                itemService.getItemById(created.getId(), outsider.getId());

        assertNull(outsiderView.getLastBooking());
        assertNull(outsiderView.getNextBooking());
    }

    @Test
    @DisplayName("Запрос предмета, которого нет → NotFoundException")
    void shouldThrowNotFoundWhenItemMissing() {
        assertThrows(NotFoundException.class,
                () -> itemService.getItemById(999L, owner.getId()));
    }
}
