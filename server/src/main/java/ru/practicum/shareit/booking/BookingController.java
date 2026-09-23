package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse create(@RequestBody BookingDto request,
                                  @RequestHeader(USER_ID_HEADER) long userId) {
        BookingResponse created = bookingService.create(request, userId);
        log.info("Создано бронирование: id={}", created.getId());
        return created;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse approve(@PathVariable long bookingId,
                                   @RequestParam boolean approved,
                                   @RequestHeader(USER_ID_HEADER) long userId) {
        BookingResponse response = bookingService.approve(bookingId, approved, userId);
        log.info("Бронирование {}: id={}", approved ? "подтверждено" : "отклонено", bookingId);
        return response;
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable long bookingId,
                                      @RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение бронирования: id={}", bookingId);
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponse> getAll(@RequestParam(defaultValue = "ALL") BookingState state,
                                        @RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение бронирований пользователя: userId={}, state={}", userId, state);
        return bookingService.findAllByBooker(state, userId);
    }


    @GetMapping("/owner")
    public List<BookingResponse> getAllByOwner(@RequestParam(defaultValue = "ALL") BookingState state,
                                               @RequestHeader(USER_ID_HEADER) long userId) {
        log.debug("Получение бронирований владельца: userId={}, state={}", userId, state);
        return bookingService.findAllByOwner(state, userId);
    }
}