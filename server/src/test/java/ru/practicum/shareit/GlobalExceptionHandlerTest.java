package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.model.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Тесты GlobalExceptionHandler")
@WebMvcTest(ru.practicum.shareit.user.UserController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("handleNotFound - 404 Not Found")
    void handleNotFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("handleForbidden - 403 Forbidden")
    void handleForbidden() throws Exception {
        when(userService.updateUser(any(UserDto.class))).thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("handleConflict - 409 Conflict")
    void handleConflict() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenThrow(new ConflictException("Email taken"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"User\", \"email\":\"test@test.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email taken"));
    }

    @Test
    @DisplayName("handleBadRequest (ValidationException) - 400 Bad Request")
    void handleValidationException() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenThrow(new ValidationException("Invalid data"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"User\", \"email\":\"test@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid data"));
    }

    @Test
    @DisplayName("handleIllegalArgument - 400 Bad Request")
    void handleIllegalArgument() throws Exception {
        when(userService.updateUser(any(UserDto.class))).thenThrow(new IllegalArgumentException("Bad params"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Неверные параметры"))
                .andExpect(jsonPath("$.message").value("Bad params"));
    }
}
