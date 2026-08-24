package ru.practicum.shareit.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.shareit.exception.dto.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.exception.model.ConflictException;
import ru.practicum.shareit.exception.model.ForbiddenException;
import ru.practicum.shareit.exception.model.NotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        log.warn("Элемент не найден: {}", ex.getMessage());
        return ErrorResponse.builder()
                .error("Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ErrorResponse handleForbidden(ForbiddenException ex) {
        log.error("Запрещено: {}", ex.getMessage());
        return ErrorResponse.builder()
                .error("Forbidden")
                .status(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ErrorResponse handleConflict(ConflictException ex) {
        log.warn("Конфликт: {}", ex.getMessage());
        return ErrorResponse.builder()
                .error("Conflict")
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(ValidationException ex) {
        log.warn("Не верный запрос: {}", ex.getMessage());
        return ErrorResponse.builder()
                .error("Bad Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
    }
}
