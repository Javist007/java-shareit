package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestDto {

    @NotBlank(message = "Описание запроса не должно быть пустым")
    private String description;
}
