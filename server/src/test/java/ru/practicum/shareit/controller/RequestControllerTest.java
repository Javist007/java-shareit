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

import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@WebMvcTest(ItemRequestController.class)
@DisplayName("MockMVC тесты для RequestController (server)")
class RequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void create() throws Exception {
        long userId = 42L;
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Free delivery");

        ItemRequestResponse resp = ItemRequestResponse.builder()
                .id(99).description("Free delivery").build();

        when(requestService.create(eq(dto), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.description").value("Free delivery"));
    }

    @Test
    void getById() throws Exception {
        long userId = 3L, reqId = 8L;
        ItemRequestResponse resp = ItemRequestResponse.builder()
                .id(reqId).build();

        when(requestService.getById(eq(reqId)))
                .thenReturn(resp);

        mockMvc.perform(get("/requests/{id}", reqId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reqId));
    }

    @Test
    void getOwn() throws Exception {
        long userId = 3L;
        when(requestService.getOwn(eq(userId)))
                .thenReturn(List.of(ItemRequestResponse.builder()
                        .id(1).build()));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAll() throws Exception {
        long userId = 3L;
        when(requestService.getAll(eq(userId)))
                .thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk());
    }

    @Test
    void notFound() throws Exception {
        when(requestService.getById(eq(999L)))
                .thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/requests/999")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());
    }
}
