package ru.practicum.shareit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient client;

    @Test
    @DisplayName("GET /bookings/{id}")
    void getById() throws Exception {
        when(client.getById(2L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(client).getById(2L, 1L);
    }

    @Test
    @DisplayName("GET /bookings?state=ALL")
    void getAll() throws Exception {
        List<BookingDto> list = List.of(new BookingDto());
        when(client.getAllByBooker(eq(1L), any())).thenReturn(ResponseEntity.ok(list));

        mockMvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /bookings – создание бронирования")
    void create() throws Exception {
        BookingDto request = new BookingDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        when(client.create(eq(1L), argThat(dto ->
                dto.getItemId() != null && dto.getStart() != null && dto.getEnd() != null)))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 10,
                        "itemId", 1,
                        "start", request.getStart(),
                        "end", request.getEnd()
                )));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(client).create(eq(1L), any());
    }

    @Test
    @DisplayName("PATCH /bookings/{id} – одобрение бронирования")
    void updateStatus() throws Exception {
        when(client.updateStatus(eq(1L), eq(2L), anyBoolean()))
                .thenReturn(ResponseEntity.ok(new BookingDto()));

        mockMvc.perform(patch("/bookings/2")
                        .param("approved", "true")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());

        verify(client).updateStatus(eq(1L), eq(2L), anyBoolean());
    }
}
