package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long bookingId) {
        log.info("Получаем бронирование {}, userId={}", bookingId, userId);
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получаем бронирования с состоянием {}, userId={}", state, userId);
        return bookingClient.getAllByBooker(userId, parseState(state));
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получаем бронирования владельца с состоянием {}, userId={}", state, userId);
        return bookingClient.getAllByOwner(userId, parseState(state));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody BookingDto bookingDto) {
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new IllegalArgumentException(
                    "Дата начала бронирования должна быть раньше даты окончания"
            );
        }
        log.info("Создаём бронирование {}, userId={}", bookingDto, userId);
        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @PathVariable Long bookingId,
                                               @RequestParam boolean approved) {
        log.info("Одобряем бронирование {}, userId={}, approved={}", bookingId, userId, approved);
        return bookingClient.updateStatus(userId, bookingId, approved);
    }

    private BookingState parseState(String state) {
        return BookingState.from(state)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное состояние: " + state));
    }
}