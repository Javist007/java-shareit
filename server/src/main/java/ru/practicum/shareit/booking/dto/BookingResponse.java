package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {

    private long id;
    private BookingStatus status;
    private ItemInfo item;
    private BookerInfo booker;
    private LocalDateTime start;
    private LocalDateTime end;


    @Data
    @Builder
    public static class ItemInfo {
        private long id;
        private String name;
    }

    @Data
    @Builder
    public static class BookerInfo {
        private long id;
        private String name;
    }
}
