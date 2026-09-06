package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.comment.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemResponse {

    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDescription lastBooking;
    private BookingDescription nextBooking;
    private List<CommentResponse> comments;

    @Data
    @Builder
    public static class BookingDescription {
        private long id;
        private long bookerId;
        private LocalDateTime start;
        private LocalDateTime end;
    }
}