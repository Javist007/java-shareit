package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Slf4j
@Validated
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RequestClient requestClient;

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long requestId) {
        log.info("Получаем запрос {}, userId={}", requestId, userId);
        return requestClient.getById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получаем запросы пользователя {}", userId);
        return requestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получаем все запросы, userId={}", userId);
        return requestClient.getAll(userId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody ItemRequestDto requestDto) {
        log.info("Создаём запрос {}, userId={}", requestDto, userId);
        return requestClient.create(userId, requestDto);
    }
}