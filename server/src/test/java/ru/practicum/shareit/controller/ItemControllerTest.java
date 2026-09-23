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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.exception.model.CommentNotAllowedException;
import ru.practicum.shareit.exception.model.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.comment.CommentResponse;
import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.item.service.comment.CommentService;
import ru.practicum.shareit.item.service.item.ItemService;

import java.util.List;

@WebMvcTest(ItemController.class)
@DisplayName("MockMVC тесты для ItemController (server)")
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;
    @MockBean
    private CommentService commentService;

    @Test
    void getAll() throws Exception {
        long userId = 7L;
        when(itemService.findAllOwnerItems(eq(userId)))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getById() throws Exception {
        long itemId = 1L, userId = 7L;
        ItemResponse resp = ItemResponse.builder()
                .id(itemId)
                .name("Drill")
                .build();
        when(itemService.getItemById(eq(itemId), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }

    @Test
    void create() throws Exception {
        long userId = 7L;
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .build();

        ItemResponse resp = ItemResponse.builder()
                .id(1L).name("Drill").build();
        when(itemService.addItem(eq(dto), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update() throws Exception {
        long itemId = 1L, userId = 7L;
        ItemDto dto = ItemDto.builder()
                .name("Updated")
                .available(false)
                .build();

        ItemResponse resp = ItemResponse.builder()
                .id(itemId).name("Updated").build();
        when(itemService.updateItem(eq(itemId), eq(dto), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void search() throws Exception {
        when(itemService.search(eq("drill")))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment() throws Exception {
        long itemId = 1L, userId = 7L;
        CommentResponse resp = CommentResponse.builder()
                .id(4L)
                .text("Works well")
                .build();
        when(commentService.addComment(eq(itemId), any(), eq(userId)))
                .thenReturn(resp);

        mockMvc.perform(post("/items/{id}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType("application/json")
                        .content("{\"text\":\"Works well\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Works well"));
    }

    @Test
    void notFound() throws Exception {
        when(itemService.getItemById(eq(999L), anyLong()))
                .thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/items/999")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Покрытие ItemNotAvailableException")
    void testItemNotAvailable() throws Exception {

        when(itemService.addItem(any(), anyLong())).thenThrow(new ItemNotAvailableException("Not available"));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\", \"description\":\"Y\", \"available\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Not available"));
    }

    @Test
    @DisplayName("Покрытие CommentNotAllowedException")
    void testCommentNotAllowed() throws Exception {

        when(commentService.addComment(anyLong(), any(), anyLong())).thenThrow(new CommentNotAllowedException("No booking"));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Hi\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("No booking"));
    }

}
