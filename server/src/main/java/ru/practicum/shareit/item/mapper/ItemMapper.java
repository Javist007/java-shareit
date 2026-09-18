package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.comment.CommentResponse;
import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

    public static ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(requestIdOf(item))
                .build();
    }

    public static ItemResponse toResponse(Item item, Booking lastBooking,
                                          Booking nextBooking, List<CommentResponse> comments) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking == null ? null : toBookingDescription(lastBooking))
                .nextBooking(nextBooking == null ? null : toBookingDescription(nextBooking))
                .comments(comments)
                .requestId(requestIdOf(item))
                .build();
    }

    public static Item toEntity(ItemDto request, ItemRequest itemRequest) {
        return Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .available(request.getAvailable())
                .request(itemRequest)
                .build();
    }

    private static ItemResponse.BookingDescription toBookingDescription(Booking booking) {
        return ItemResponse.BookingDescription.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    private static Long requestIdOf(Item item) {
        return item.getRequest() == null ? null : item.getRequest().getId();
    }
}
