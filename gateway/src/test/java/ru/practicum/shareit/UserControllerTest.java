package ru.practicum.shareit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient client;

    @Test
    @DisplayName("POST /users - создаёт пользователя")
    void createUser() throws Exception {
        when(client.create(argThat(user -> "user@example.com".equals(user.getEmail()))))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 1,
                        "name", "User",
                        "email", "user@example.com"
                )));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "User",
                                "email", "user@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(client).create(argThat(user ->
                "User".equals(user.getName()) && "user@example.com".equals(user.getEmail())));
    }

    @Test
    @DisplayName("PATCH /users/{id} - обновляет пользователя")
    void updateUser() throws Exception {
        when(client.update(eq(1L), argThat(user -> "Updated".equals(user.getName()))))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 1,
                        "name", "Updated"
                )));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("name", "Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(client).update(eq(1L), argThat(user -> "Updated".equals(user.getName())));
    }

    @Test
    @DisplayName("GET /users/{id} - получает пользователя")
    void getUser() throws Exception {
        when(client.getById(1L)).thenReturn(ResponseEntity.ok(Map.of(
                "id", 1,
                "name", "User",
                "email", "user@example.com"
        )));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(client).getById(1L);
    }

    @Test
    @DisplayName("DELETE /users/{id} - удаляет пользователя")
    void deleteUser() throws Exception {
        when(client.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(client).delete(1L);
    }
}
