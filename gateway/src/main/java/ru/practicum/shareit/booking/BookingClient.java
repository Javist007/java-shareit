package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByBooker(Long userId, BookingState state) {
        return get("?state={state}", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, BookingState state) {
        return get("/owner?state={state}", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> create(Long userId, BookingDto bookingCreateDto) {
        return post("", userId, bookingCreateDto);
    }

    public ResponseEntity<Object> updateStatus(Long userId, Long bookingId, boolean approved) {
        return patch("/" + bookingId + "?approved={approved}", userId,
                Map.of("approved", approved), null);
    }
}