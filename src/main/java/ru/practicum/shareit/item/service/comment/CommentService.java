package ru.practicum.shareit.item.service.comment;

import ru.practicum.shareit.item.dto.comment.CommentDto;
import ru.practicum.shareit.item.dto.comment.CommentResponse;

public interface CommentService {

    CommentResponse addComment(long itemId, CommentDto request, long userId);
}
