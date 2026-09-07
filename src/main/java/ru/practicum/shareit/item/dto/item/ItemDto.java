package ru.practicum.shareit.item.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {

    private Long id;

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(max = 255, message = "Имя слишком длинное")
    private String name;

    @NotBlank(message = "Описание не должно быть пустым")
    @Size(max = 512, message = "Описание слишком длинное")
    private String description;

    @NotNull(message = "Флаг availability обязателен")
    private Boolean available;
}
