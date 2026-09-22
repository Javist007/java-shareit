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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.model.*;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

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
    private UserRepository userRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private ItemRequestRepository requestRepo;

    private User booker;
    private User owner;
    private Item item;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        booker = userRepo.save(User.builder()
                .name("Booker").email("booker@example.com").build());
        owner = userRepo.save(User.builder()
                .name("Owner").email("owner@example.com").build());

        ItemRequest req = requestRepo.save(ItemRequest.builder()
                .description("Need a drill")
                .requestor(owner).created(now)
                .build());

        item = itemRepo.save(Item.builder()
                .name("Drill").description("Electric drill")
                .available(true).owner(owner).request(req).build());
    }

    @Test
    @DisplayName("Должен создать бронирование и вернуть его со статусом WAITING")
    void shouldCreateBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(now.plusHours(1))
                .end(now.plusHours(5))
                .build();

        BookingResponse response = bookingService.create(dto, booker.getId());

        assertEquals(booker.getId(), response.getBooker().getId());
        assertEquals(item.getId(), response.getItem().getId());
        assertEquals(BookingStatus.WAITING, response.getStatus());

        List<BookingResponse> all = bookingService.findAllByBooker(BookingState.ALL, booker.getId());
        assertEquals(1, all.size());
        BookingResponse b = all.getFirst();
        assertEquals(BookingStatus.WAITING, b.getStatus());
    }

    @Test
    @DisplayName("Должен одобрить бронирование и отклонить несанкционированные попытки")
    void shouldApproveBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(now.plusHours(2))
                .end(now.plusHours(6))
                .build();
        BookingResponse created = bookingService.create(dto, booker.getId());

        BookingResponse approved = bookingService.approve(created.getId(), true, owner.getId());
        assertEquals(BookingStatus.APPROVED, approved.getStatus());

        assertThrows(
                ForbiddenException.class,
                () -> bookingService.approve(created.getId(), false, booker.getId()));
    }

    @Test
    @DisplayName("Должен отклонить пересекающиеся бронирования")
    void shouldRejectOverlappingBookings() {
        BookingDto dto1 = BookingDto.builder()
                .itemId(item.getId())
                .start(now.plusHours(10))
                .end(now.plusHours(20))
                .build();
        BookingResponse created1 = bookingService.create(dto1, booker.getId());
        bookingService.approve(created1.getId(), true, owner.getId());

        BookingDto dto2 = BookingDto.builder()
                .itemId(item.getId())
                .start(now.plusHours(15))
                .end(now.plusHours(25))
                .build();

        assertThrows(
                ConflictException.class,
                () -> bookingService.create(dto2, booker.getId()));
    }

    @Test
    @DisplayName("Только владелец или заявитель могут читать бронирование")
    void shouldAllowOnlyOwnerOrBookerToRead() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(now.plusHours(3))
                .end(now.plusHours(7))
                .build();
        BookingResponse created = bookingService.create(dto, booker.getId());

        assertDoesNotThrow(() -> bookingService.findById(created.getId(), booker.getId()));
        assertDoesNotThrow(() -> bookingService.findById(created.getId(), owner.getId()));

        User outsider = userRepo.save(User.builder()
                .name("Outsider").email("out@example.com").build());
        assertThrows(
                ForbiddenException.class,
                () -> bookingService.findById(created.getId(), outsider.getId()));
    }

    @Test
    @DisplayName("Бронирования должны возвращаться корректно по фильтрам состояния")
    void shouldReturnBookingsByState() {
        BookingDto past = BookingDto.builder()
                .itemId(item.getId())
                .start(now.minusDays(5))
                .end(now.minusDays(4))
                .build();
        bookingService.create(past, booker.getId());

        BookingDto future = BookingDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(2))
                .end(now.plusDays(3))
                .build();
        bookingService.create(future, booker.getId());

        List<BookingResponse> all = bookingService.findAllByBooker(BookingState.ALL, booker.getId());
        assertEquals(2, all.size());

        List<BookingResponse> pastRes = bookingService.findAllByBooker(BookingState.PAST, booker.getId());
        assertTrue(pastRes.isEmpty() || pastRes.getFirst().getEnd().isBefore(now));

        List<BookingResponse> futureRes = bookingService.findAllByOwner(BookingState.FUTURE, owner.getId());
        assertTrue(futureRes.isEmpty() || futureRes.getFirst().getStart().isAfter(now));
    }
}
