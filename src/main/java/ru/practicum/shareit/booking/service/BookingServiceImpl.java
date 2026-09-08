package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.model.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponse findById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getBooker().getId() != userId &&
            booking.getItem().getOwner().getId() != userId) {
            throw new ForbiddenException("Недостаточно прав");
        }
        return BookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse create(BookingDto request, long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна");
        }

        if (request.getEnd().isBefore(request.getStart())
            || request.getEnd().equals(request.getStart())) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }

        if (item.getOwner().getId() == userId) {
            throw new ConflictException("Владелец не может забронировать собственный товар");
        }

        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
                request.getItemId(), request.getStart(), request.getEnd());
        if (isOverlapping) {
            throw new ConflictException("Товар уже забронирован на этот период времени");
        }

        Booking booking = BookingMapper.toEntity(request, item, booker);
        Booking saved = bookingRepository.save(booking);

        log.info("Бронирование зарегистрировано: id={}, userId={}, itemId={}",
                saved.getId(), userId, request.getItemId());

        return BookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse approve(long bookingId, Boolean approved, long userId) {
        if (approved == null) {
            throw new ValidationException("Параметр 'approved' обязателен");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (booking.getItem().getOwner().getId() != userId) {
            throw new ForbiddenException("Только владелец может подтверждать");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Бронирование не находится в состоянии WAITING");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);

        log.info("Бронирование {}: id={}, userId={}", approved ? "одобрено" : "отклонено", bookingId, userId);

        return BookingMapper.toResponse(saved);
    }

    @Override
    public List<BookingResponse> findAllByBooker(BookingState state, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookings = bookingRepository.findCurrentByBookerId(userId, LocalDateTime.now());
            case PAST -> bookings = bookingRepository.findPastByBookerId(userId, LocalDateTime.now());
            case FUTURE -> bookings = bookingRepository.findFutureByBookerId(userId, LocalDateTime.now());
            case WAITING -> bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED);
            default -> throw new ValidationException("Недопустимое состояние бронирования: " + state);
        }

        return getBookingResponses("findAllByBooker", state, bookings);
    }

    @Override
    public List<BookingResponse> findAllByOwner(BookingState state, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookings = bookingRepository.findCurrentByOwnerId(userId, LocalDateTime.now());
            case PAST -> bookings = bookingRepository.findPastByOwnerId(userId, LocalDateTime.now());
            case FUTURE -> bookings = bookingRepository.findFutureByOwnerId(userId, LocalDateTime.now());
            case WAITING -> bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED);
            default -> throw new ValidationException("Недопустимое состояние бронирования: " + state);
        }

        return getBookingResponses("findAllByOwner", state, bookings);
    }

    public static Booking getLastBooking(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .findFirst()
                .orElse(null);
    }

    public static Booking getNextBooking(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private static List<BookingResponse> getBookingResponses(String methodName, BookingState state, List<Booking> bookings) {
        log.info("{} '{}' возвращает {} записей", methodName, state, bookings.size());
        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }
}
