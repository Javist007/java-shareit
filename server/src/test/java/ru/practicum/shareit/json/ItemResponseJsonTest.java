package ru.practicum.shareit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.comment.CommentResponse;
import ru.practicum.shareit.item.dto.item.ItemResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonTest тесты для ItemResponse")
@JsonTest
class ItemResponseJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesBookingsAndComments() throws Exception {

        ItemResponse response = ItemResponse.builder()
                .id(2L)
                .name("Drill")
                .description("electric drill")
                .available(false)
                .requestId(8L)
                .lastBooking(ItemResponse.BookingDescription.builder()
                        .id(3L)
                        .bookerId(4L)
                        .start(LocalDateTime.of(2025, 12, 5, 10, 0, 0))
                        .end(LocalDateTime.of(2025, 12, 6, 11, 30, 0))
                        .build())
                .comments(List.of(CommentResponse.builder()
                        .id(2L)
                        .text("Excellent")
                        .authorName("Bob")
                        .created(LocalDateTime.of(2025, 12, 4, 14, 45, 0))
                        .build()))
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"requestId\":8");
        assertThat(json).contains("\"lastBooking\":{\"id\":3");
        assertThat(json).contains("\"start\":\"2025-12-05T10:00:00\"");
        assertThat(json).contains("\"comments\":[{\"id\":2");
        assertThat(json).contains("\"authorName\":\"Bob\"");
    }
}
