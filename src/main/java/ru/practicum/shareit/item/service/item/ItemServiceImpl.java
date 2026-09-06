package ru.practicum.shareit.item.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.model.ForbiddenException;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.item.dto.comment.CommentResponse;
import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemResponse addItem(ItemDto request, long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toEntity(request);
        item.setOwner(owner);

        log.debug("Создание вещи: ownerId={}, name={}", ownerId, request.getName());
        return ItemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    public ItemResponse updateItem(long itemId, ItemDto request, long ownerId) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (existing.getOwner().getId() != ownerId) {
            throw new ForbiddenException("Только владелец может обновить элемент");
        }

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getAvailable() != null) {
            existing.setAvailable(request.getAvailable());
        }

        log.info("Обновлена вещь {}: новые данные = {}", itemId, existing);
        return ItemMapper.toResponse(itemRepository.save(existing));
    }

    @Override
    public ItemResponse getItemById(long id, long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        List<CommentResponse> comments = commentRepository.findByItemId(id).stream()
                .map(CommentMapper::toResponse)
                .toList();

        Booking last = null;
        Booking next = null;

        if (item.getOwner().getId() == userId) {
            List<Booking> allApproved = bookingRepository.findAllApprovedForItems(List.of(id));
            last = BookingServiceImpl.getLastBooking(allApproved);
            next = BookingServiceImpl.getNextBooking(allApproved);
        }

        log.debug("Получаем детали вещи id={}", id);
        return ItemMapper.toResponse(item, last, next, comments);
    }

    @Override
    public List<ItemResponse> findAllOwnerItems(long ownerId) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        List<Booking> allApproved = bookingRepository.findAllApprovedForItems(itemIds);

        Map<Long, Booking> lastBookings = new HashMap<>();
        Map<Long, Booking> nextBookings = new HashMap<>();

        Map<Long, List<Booking>> byItemId = allApproved.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        byItemId.forEach((itemId, bookings) -> {
            Booking last = BookingServiceImpl.getLastBooking(bookings);
            Booking next = BookingServiceImpl.getNextBooking(bookings);
            if (last != null) lastBookings.put(itemId, last);
            if (next != null) nextBookings.put(itemId, next);
        });

        Map<Long, List<CommentResponse>> commentsMap = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toResponse, Collectors.toList())
                ));

        return items.stream()
                .map(item -> ItemMapper.toResponse(item,
                        lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()),
                        commentsMap.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @Override
    public List<ItemResponse> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toResponse)
                .toList();
    }
}
