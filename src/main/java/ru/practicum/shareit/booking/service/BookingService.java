package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.enums.BookingState;

import java.util.List;

public interface BookingService {

    BookingResponse findById(long bookingId, long userId);

    List<BookingResponse> findAllByBooker(BookingState state, long userId);

    List<BookingResponse> findAllByOwner(BookingState state, long userId);

    BookingResponse create(BookingDto request, long userId);

    BookingResponse approve(long bookingId, Boolean approved, long userId);
}
