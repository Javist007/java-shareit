package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import jakarta.persistence.EntityManager;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.model.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для BookingService")
@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = persist(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = persist(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        item = new Item();
        item.setName("Drill");
        item.setDescription("Cordless drill");
        item.setAvailable(true);
        item.setOwner(owner);

        persist(item);
    }

    @Test
    @DisplayName("Создание бронирования успешно")
    void testCreateBookingSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingResponse response =
                bookingService.create(bookingDto(start, end), booker.getId());

        assertNotNull(response);
        assertEquals(BookingStatus.WAITING,
                response.getStatus());
        assertEquals(item.getId(), response.getItem().getId());
        assertEquals(booker.getId(), response.getBooker().getId());
        assertEquals(1L, bookingRepository.count());
    }

    @Test
    @DisplayName("Создание бронирования с недоступным предметом выбрасывает ItemNotAvailableException")
    void testCreateBookingItemNotAvailable() {
        item.setAvailable(false);
        entityManager.merge(item);
        entityManager.flush();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        assertThrows(
                ItemNotAvailableException.class,
                () -> bookingService.create(bookingDto(start, end), booker.getId()));
    }

    @Test
    @DisplayName("Создание бронирования собственным предметом выбрасывает ConflictException")
    void testCreateBookingOwnItemConflict() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        assertThrows(
                ConflictException.class,
                () -> bookingService.create(bookingDto(start, end), owner.getId()));
    }

    @Test
    @DisplayName("Создание бронирования с неверными датами выбрасывает ValidationException")
    void testCreateBookingInvalidDates() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThrows(
                ValidationException.class,
                () -> bookingService.create(bookingDto(start, end), booker.getId()));
    }

    @Test
    @DisplayName("Утверждение бронирования успешно")
    void testApproveBookingSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingResponse created =
                bookingService.create(bookingDto(start, end), booker.getId());

        BookingResponse approved =
                bookingService.approve(created.getId(), true, owner.getId());

        assertEquals(BookingStatus.APPROVED,
                approved.getStatus());
    }

    @Test
    @DisplayName("Отклонение бронирования")
    void testApproveBookingReject() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingResponse created =
                bookingService.create(bookingDto(start, end), booker.getId());

        BookingResponse rejected =
                bookingService.approve(created.getId(), false, owner.getId());

        assertEquals(BookingStatus.REJECTED,
                rejected.getStatus());
    }

    @Test
    @DisplayName("Невозможно утвердить бронирование чужим пользователем")
    void testApproveForbiddenOwner() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingResponse created =
                bookingService.create(bookingDto(start, end), booker.getId());

        assertThrows(
                ForbiddenException.class,
                () -> bookingService.approve(created.getId(), true, booker.getId()));
    }

    @Test
    @DisplayName("Попытка повторного утверждения уже одобренного бронирования выбрасывает ConflictException")
    void testApproveAlreadyApproved() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingResponse created =
                bookingService.create(bookingDto(start, end), booker.getId());

        bookingService.approve(created.getId(), true, owner.getId());

        assertThrows(
                ConflictException.class,
                () -> bookingService.approve(created.getId(), false, owner.getId()));
    }

    @Test
    @DisplayName("Получение бронирования с разрешением (владелец или бронирующий)")
    void testFindByIdWithPermission() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingResponse created =
                bookingService.create(bookingDto(start, end), booker.getId());

        assertEquals(created.getId(),
                bookingService.findById(created.getId(), booker.getId()).getId());
        assertEquals(created.getId(),
                bookingService.findById(created.getId(), owner.getId()).getId());

        User outsider =
                persist(User.builder()
                        .name("Other")
                        .email("other@example.com")
                        .build());

        assertThrows(
                ForbiddenException.class,
                () -> bookingService.findById(created.getId(), outsider.getId()));
    }

    @Test
    @DisplayName("Получение всех бронирований по состояниям для бронирующего")
    void testFindAllByBookerStates() {
        LocalDateTime now = LocalDateTime.now();

        bookingService.create(bookingDto(now.minusDays(5), now.minusDays(4)), booker.getId());

        bookingService.create(bookingDto(now.minusHours(1), now.plusHours(1)), booker.getId());

        bookingService.create(bookingDto(now.plusDays(2), now.plusDays(3)), booker.getId());

        BookingResponse rejected =
                bookingService.create(bookingDto(now.minusDays(10), now.minusDays(9)),
                        booker.getId());
        bookingService.approve(rejected.getId(), false, owner.getId());

        BookingResponse approved =
                bookingService.create(bookingDto(now.minusHours(2), now.plusHours(2)),
                        booker.getId());
        bookingService.approve(approved.getId(), true, owner.getId());

        List<BookingResponse> all =
                bookingService.findAllByBooker(
                        BookingState.ALL,
                        booker.getId());
        assertEquals(5, all.size());

        List<BookingResponse> current =
                bookingService.findAllByBooker(
                        BookingState.CURRENT,
                        booker.getId());
        assertEquals(2, current.size());

        List<BookingResponse> past =
                bookingService.findAllByBooker(
                        BookingState.PAST,
                        booker.getId());
        assertEquals(2, past.size());

        List<BookingResponse> future =
                bookingService.findAllByBooker(
                        BookingState.FUTURE,
                        booker.getId());
        assertEquals(1, future.size());

        List<BookingResponse> waiting =
                bookingService.findAllByBooker(
                        BookingState.WAITING,
                        booker.getId());
        assertEquals(3, waiting.size());


        List<BookingResponse> rejectedList =
                bookingService.findAllByBooker(
                        BookingState.REJECTED,
                        booker.getId());
        assertEquals(1, rejectedList.size());
    }

    @Test
    @DisplayName("Получение всех бронирований по состояниям для владельца")
    void testFindAllByOwnerStates() {
        LocalDateTime now = LocalDateTime.now();

        bookingService.create(bookingDto(now.minusDays(5), now.minusDays(4)), booker.getId());

        bookingService.create(bookingDto(now.minusHours(1), now.plusHours(1)), booker.getId());

        bookingService.create(bookingDto(now.plusDays(2), now.plusDays(3)), booker.getId());

        BookingResponse rejected =
                bookingService.create(bookingDto(now.minusDays(10), now.minusDays(9)),
                        booker.getId());
        bookingService.approve(rejected.getId(), false, owner.getId());

        BookingResponse approved =
                bookingService.create(bookingDto(now.minusHours(2), now.plusHours(2)),
                        booker.getId());
        bookingService.approve(approved.getId(), true, owner.getId());

        List<BookingResponse> all =
                bookingService.findAllByOwner(
                        BookingState.ALL,
                        owner.getId());
        assertEquals(5, all.size());

        List<BookingResponse> current =
                bookingService.findAllByOwner(
                        BookingState.CURRENT,
                        owner.getId());
        assertEquals(2, current.size());

        List<BookingResponse> past =
                bookingService.findAllByOwner(
                        BookingState.PAST,
                        owner.getId());
        assertEquals(2, past.size());

        List<BookingResponse> future =
                bookingService.findAllByOwner(
                        BookingState.FUTURE,
                        owner.getId());
        assertEquals(1, future.size());

        List<BookingResponse> waiting =
                bookingService.findAllByOwner(
                        BookingState.WAITING,
                        owner.getId());
        assertEquals(3, waiting.size());

        List<BookingResponse> rejectedList =
                bookingService.findAllByOwner(
                        BookingState.REJECTED,
                        owner.getId());
        assertEquals(1, rejectedList.size());
    }

    @Test
    @DisplayName("Создание бронирования для несуществующего пользователя -> NotFoundException")
    void testCreateBookingNonExistentUser() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingDto(start, end), 999L));
    }

    @Test
    @DisplayName("Создание бронирования для несуществующего товара -> NotFoundException")
    void testCreateBookingNonExistentItem() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        assertThrows(NotFoundException.class,
                () -> bookingService.create(BookingDto.builder()
                        .itemId(999L)
                        .start(start)
                        .end(end)
                        .build(), booker.getId()));
    }

    @Test
    @DisplayName("Создание бронирования с пересекающимися датами -> ConflictException")
    void testCreateBookingOverlapping() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(3);

        BookingResponse first = bookingService.create(bookingDto(start1, end1), booker.getId());

        bookingService.approve(first.getId(), true, owner.getId());

        LocalDateTime start2 = LocalDateTime.now().plusDays(2);
        LocalDateTime end2 = LocalDateTime.now().plusDays(4);

        assertThrows(ConflictException.class,
                () -> bookingService.create(bookingDto(start2, end2), booker.getId()));
    }

    @Test
    @DisplayName("Проверка всех состояний findAllByBooker")
    void testAllBookingStatesCoverage() {
        LocalDateTime now = LocalDateTime.now();

        bookingService.create(bookingDto(now.minusDays(5), now.minusDays(4)), booker.getId());

        bookingService.create(bookingDto(now.plusDays(2), now.plusDays(3)), booker.getId());

        bookingService.create(bookingDto(now.minusHours(1), now.plusHours(1)), booker.getId());

        bookingService.create(bookingDto(now.plusDays(10), now.plusDays(11)), booker.getId());

        BookingResponse rejected = bookingService.create(bookingDto(now.minusDays(10), now.minusDays(9)), booker.getId());
        bookingService.approve(rejected.getId(), false, owner.getId());

        assertFalse(bookingService.findAllByBooker(BookingState.ALL, booker.getId()).isEmpty());
        assertFalse(bookingService.findAllByBooker(BookingState.CURRENT, booker.getId()).isEmpty());
        assertFalse(bookingService.findAllByBooker(BookingState.PAST, booker.getId()).isEmpty());
        assertFalse(bookingService.findAllByBooker(BookingState.FUTURE, booker.getId()).isEmpty());
        assertFalse(bookingService.findAllByBooker(BookingState.WAITING, booker.getId()).isEmpty());
        assertFalse(bookingService.findAllByBooker(BookingState.REJECTED, booker.getId()).isEmpty());
    }

    @SuppressWarnings("uncheck")
    private <T> T persist(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private BookingDto bookingDto(LocalDateTime start,
                                  LocalDateTime end) {
        return BookingDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();
    }
}
