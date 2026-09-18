package ru.practicum.shareit.item.service.comment;

import ru.practicum.shareit.item.dto.comment.CommentCreateDto;
import ru.practicum.shareit.item.dto.comment.CommentResponse;

public interface CommentService {

    CommentResponse addComment(long itemId, CommentCreateDto request, long userId);
}
