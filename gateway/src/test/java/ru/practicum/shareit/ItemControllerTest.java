package ru.practicum.shareit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient client;

    @Test
    @DisplayName("GET /items – список всех предметов")
    void getAll() throws Exception {
        when(client.getAll(7L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 7))
                .andExpect(status().isOk());

        verify(client).getAll(7L);
    }

    @Test
    @DisplayName("GET /items/{id}")
    void getById() throws Exception {
        when(client.getById(7L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/items/1").header(USER_ID_HEADER, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(client).getById(7L, 1L);
    }

    @Test
    @DisplayName("POST /items – создание предмета")
    void create() throws Exception {
        when(client.create(eq(7L), argThat(item -> "Drill".equals(item.getName()))))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 1,
                        "name", "Drill",
                        "description", "Cordless drill",
                        "available", true
                )));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemDto.builder()
                                .name("Drill")
                                .description("Cordless drill")
                                .available(true)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(client).create(eq(7L), argThat(item ->
                "Drill".equals(item.getName()) && Boolean.TRUE.equals(item.getAvailable())));
    }

    @Test
    @DisplayName("PATCH /items/{id} – обновление предмета")
    void update() throws Exception {
        when(client.update(eq(7L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Updated")));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ItemDto.builder()
                                .name("Updated")
                                .description("Some desc")
                                .available(true)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(client).update(eq(7L), eq(1L),
                argThat(item -> "Updated".equals(item.getName())));
    }

    @Test
    @DisplayName("POST /items/{id}/comment – добавление комментария")
    void addComment() throws Exception {
        when(client.addComment(eq(7L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 4, "text", "Works well")));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Works well\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Works well"));

        verify(client).addComment(eq(7L), eq(1L),
                argThat(comment -> "Works well".equals(comment.getText())));
    }
}
