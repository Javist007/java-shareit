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

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.user.service.UserService;

@DisplayName("MockMVC тесты для UserController (server)")
@WebMvcTest(UserController.class)
@ContextConfiguration(classes = ShareItServer.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private UserService userService;

    @Test
    void create() throws Exception {
        UserDto dto = UserDto.builder()
                .name("User")
                .email("user@example.com")
                .build();

        UserResponse resp = UserResponse.builder()
                .id(1L)
                .name("User")
                .email("user@example.com")
                .build();

        when(userService.createUser(eq(dto)))
                .thenReturn(resp);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void update() throws Exception {
        long id = 1L;
        UserDto dto = UserDto.builder()
                .name("Updated")
                .build();

        UserResponse resp = UserResponse.builder()
                .id(id)
                .name("Updated")
                .email("old@example.com")
                .build();

        when(userService.updateUser(argThat(u -> u.getId() != null && u.getId().equals(id))))
                .thenReturn(resp);

        mockMvc.perform(patch("/users/{id}", id)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void getById() throws Exception {
        long id = 1L;
        UserResponse resp = UserResponse.builder()
                .id(id)
                .name("User")
                .email("user@example.com")
                .build();

        when(userService.getUserById(eq(id)))
                .thenReturn(resp);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void delete() throws Exception {
        long id = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void notFound() throws Exception {
        when(userService.getUserById(eq(999L)))
                .thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }
}
