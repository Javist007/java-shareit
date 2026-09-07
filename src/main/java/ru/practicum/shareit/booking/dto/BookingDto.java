package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {

    @NotNull(message = "itemId обязателен")
    @Positive(message = "itemId должен быть > 0")
    private Long itemId;

    @NotNull(message = "Дата начала обязательна")
    @FutureOrPresent(message = "start должно быть в настоящем или будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна")
    @Future(message = "end должно быть после now")
    private LocalDateTime end;
}
