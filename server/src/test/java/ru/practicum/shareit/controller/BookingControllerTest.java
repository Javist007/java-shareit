package ru.practicum.shareit.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.model.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(BookingController.class)
@DisplayName("MockMVC тесты для BookingController (server)")
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void getById() throws Exception {
        long bookingId = 1L, userId = 2L;
        BookingResponse resp = BookingResponse.builder()
                .id(bookingId)
                .status(null)
                .build();

        when(bookingService.findById(eq(bookingId), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(get("/bookings/{id}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void getAllByBooker() throws Exception {
        long userId = 1L;
        BookingResponse r1 = BookingResponse.builder()
                .id(10).build();
        when(bookingService.findAllByBooker(eq(BookingState.ALL), eq(userId)))
                .thenReturn(List.of(r1));

        mockMvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void create() throws Exception {
        long userId = 1L;
        BookingDto dto = BookingDto.builder()
                .itemId(5L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponse resp = BookingResponse.builder()
                .id(100).build();
        when(bookingService.create(eq(dto), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void approve() throws Exception {
        long bookingId = 5L, userId = 1L;
        BookingResponse resp = BookingResponse.builder()
                .id(bookingId).build();
        when(bookingService.approve(eq(bookingId), eq(true), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .param("approved", "true")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void notFound() throws Exception {
        when(bookingService.findById(eq(999L), anyLong()))
                .thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/bookings/999")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());
    }
}
