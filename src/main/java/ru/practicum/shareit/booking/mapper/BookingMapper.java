package ru.practicum.shareit.booking.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor
public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .item(BookingResponse.ItemInfo.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .booker(BookingResponse.BookerInfo.builder()
                        .id(booking.getBooker().getId())
                        .name(booking.getBooker().getName())
                        .build())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static Booking toEntity(BookingDto request, Item item, User booker) {
        return Booking.builder()
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(booker)
                .start(request.getStart())
                .end(request.getEnd())
                .build();
    }
}
