package ru.practicum.shareit.item.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank(message = "Текст не должен быть пустым")
    @Size(max = 2000, message = "Текст слишком длинный (max 2000)")
    private String text;
}
