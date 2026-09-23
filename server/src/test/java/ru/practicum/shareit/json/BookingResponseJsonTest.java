package ru.practicum.shareit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Json тесты для BookingResponse")
@JsonTest
class BookingResponseJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesDatesInIsoFormat() throws Exception {

        BookingResponse response = BookingResponse.builder()
                .id(10L)
                .start(LocalDateTime.of(2025, 12, 1, 8, 15, 30))
                .end(LocalDateTime.of(2025, 12, 2, 9, 20, 45))
                .item(BookingResponse.ItemInfo.builder()
                        .id(5L)
                        .name("Screwdriver")
                        .build())
                .booker(BookingResponse.BookerInfo.builder()
                        .id(7L)
                        .name("Alice")
                        .build())
                .status(BookingStatus.WAITING)
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"start\":\"2025-12-01T08:15:30\"");
        assertThat(json).contains("\"end\":\"2025-12-02T09:20:45\"");
        assertThat(json).contains("\"status\":\"WAITING\"");
        assertThat(json).contains("\"name\":\"Screwdriver\"");
    }
}
