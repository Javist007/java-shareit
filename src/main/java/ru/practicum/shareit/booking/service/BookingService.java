package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse findById(long bookingId, long userId);

    List<BookingResponse> findAllByBooker(String state, long userId);

    List<BookingResponse> findAllByOwner(String state, long userId);

    BookingResponse create(BookingDto request, long userId);

    BookingResponse approve(long bookingId, boolean approved, long userId);
}
