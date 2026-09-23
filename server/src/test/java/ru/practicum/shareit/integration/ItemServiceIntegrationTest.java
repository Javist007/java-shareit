package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.comment.CommentCreateDto;
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
import java.util.List;

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

    @Test
    @DisplayName("Поиск с пустой строкой возвращает пустой список (early‑return)")
    void searchWithEmptyTextReturnsEmptyList() {
        List<ItemResponse> result = itemService.search("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Обновление предмета не владельцем -> ForbiddenException")
    void testUpdateItemByNonOwner() {
        ItemDto dto = ItemDto.builder()
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .build();

        ItemResponse created = itemService.addItem(dto, owner.getId());
        long itemId = created.getId();

        ItemDto updateDto = ItemDto.builder()
                .name("New Name")
                .description("New Description")
                .available(true)
                .build();

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(itemId, updateDto, outsider.getId()));
    }

    @Test
    @DisplayName("Получение предмета по несуществующему ID -> NotFoundException")
    void testGetItemByIdNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemService.getItemById(999L, owner.getId()));
    }

    @Test
    @DisplayName("Поиск предмета по частичному совпадению в описании")
    void testSearchPartialDescription() {
        ItemDto dto = ItemDto.builder()
                .name("UniqueName")
                .description("Specific description for searching")
                .available(true)
                .build();
        itemService.addItem(dto, owner.getId());

        List<ItemResponse> result = itemService.search("description for searching");
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Получение списка вещей владельца, у которого нет вещей (пустой список)")
    void testFindAllOwnerItemsEmpty() {
        User newOwner = userRepo.save(User.builder()
                .name("NoItems").email("noitems@example.com").build());

        List<ItemResponse> result = itemService.findAllOwnerItems(newOwner.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName(" findAllOwnerItems - Пользователь не найден")
    void findAllOwnerItems_UserNotFound() {
        long nonExistentId = 999L;
        assertThrows(NotFoundException.class,
                () -> itemService.findAllOwnerItems(nonExistentId));
    }

    @Test
    @DisplayName("findAllOwnerItems - Вещей нет (пустой список)")
    void findAllOwnerItems_NoItems() {
        User newUser = userRepo.save(User.builder()
                .name("User No Items")
                .email("no_items_user_" + System.currentTimeMillis() + "@example.com")
                .build());

        List<ItemResponse> result = itemService.findAllOwnerItems(newUser.getId());

        assertTrue(result.isEmpty(), "Список должен быть пустым, так как у пользователя нет вещей");
    }

    @Test
    @DisplayName(" findAllOwnerItems - Полное покрытие (есть вещи, есть бронирования и комментарии)")
    void findAllOwnerItems_FullCoverage() {

        User owner = userRepo.save(User.builder()
                .name("Owner")
                .email("owner_full@example.com")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Full Item")
                .description("Full Description")
                .available(true)
                .build();
        ItemResponse created = itemService.addItem(dto, owner.getId());
        long itemId = created.getId();

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("Great item!");

        List<ItemResponse> result = itemService.findAllOwnerItems(owner.getId());

        assertFalse(result.isEmpty());
        assertEquals("Full Item", result.getFirst().getName());
    }
}
