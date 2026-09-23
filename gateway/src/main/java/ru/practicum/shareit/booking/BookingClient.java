package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

public class BookingClient extends BaseClient {
    public static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(RestTemplate restTemplate) {
        super(restTemplate);
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