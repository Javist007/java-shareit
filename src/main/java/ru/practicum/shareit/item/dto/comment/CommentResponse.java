package ru.practicum.shareit.item.dto.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}
