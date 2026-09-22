package ru.practicum.shareit;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.request.RequestClient;
import ru.practicum.shareit.request.RequestController;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestClient client;

    @Test
    @DisplayName("POST /requests – создание запроса")
    void create() throws Exception {
        when(client.create(eq(42L), argThat(req ->
                "Free delivery".equals(req.getDescription()))))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 99,
                        "description", "Free delivery"
                )));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 42)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Free delivery\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.description").value("Free delivery"));

        verify(client).create(eq(42L), argThat(req ->
                "Free delivery".equals(req.getDescription())));
    }

    @Test
    @DisplayName("GET /requests/{id}")
    void getById() throws Exception {
        when(client.getById(3L, 8L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 8)));

        mockMvc.perform(get("/requests/8")
                        .header(USER_ID_HEADER, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));

        verify(client).getById(3L, 8L);
    }

    @Test
    @DisplayName("GET /requests – собственные запросы")
    void getOwn() throws Exception {
        when(client.getOwn(3L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(client).getOwn(3L);
    }

    @Test
    @DisplayName("GET /requests/all – чужие запросы")
    void getAll() throws Exception {
        when(client.getAll(3L))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 3))
                .andExpect(status().isOk());

        verify(client).getAll(3L);
    }
}
