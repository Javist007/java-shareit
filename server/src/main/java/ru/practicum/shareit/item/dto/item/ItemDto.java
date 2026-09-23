package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
