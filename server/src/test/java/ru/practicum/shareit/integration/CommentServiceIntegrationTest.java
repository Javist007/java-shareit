package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.item.dto.comment.CommentCreateDto;
import ru.practicum.shareit.item.dto.comment.CommentResponse;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.model.*;
import ru.practicum.shareit.item.dto.item.ItemDto;
import ru.practicum.shareit.item.dto.item.ItemResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.comment.CommentService;
import ru.practicum.shareit.item.service.item.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для CommentService")
@SpringBootTest
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;
    @Autowired
    private BookingServiceImpl bookingService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private ItemService itemService;

    private User booker, owner;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        booker = userRepo.save(User.builder()
                .name("Booker").email("booker@example.com").build());
        owner = userRepo.save(User.builder()
                .name("Owner").email("owner@example.com").build());

        itemRepo.save(Item.builder()
                .name("Drill").description("Electric drill")
                .available(true).owner(owner)
                .build());
    }

    @Test
    @DisplayName("Создание комментария после подтверждённого бронирования")
    void shouldAddCommentWhenBookingExists() {
        ItemDto dto = ItemDto.builder()
                .name("Hammer")
                .description("Steel hammer")
                .available(true)
                .build();
        ItemResponse added = itemService.addItem(dto, owner.getId());

        BookingDto bookingDto = BookingDto.builder()
                .itemId(added.getId())
                .start(now.minusHours(5))
                .end(now.minusHours(2))
                .build();
        BookingResponse bResp = bookingService.create(bookingDto, booker.getId());
        bookingService.approve(bResp.getId(), true, owner.getId());

        CommentCreateDto commentReq = new CommentCreateDto();
        commentReq.setText("Great tool!");

        CommentResponse resp = commentService.addComment(
                added.getId(), commentReq, booker.getId());
        assertEquals("Great tool!", resp.getText());
    }

    @Test
    @DisplayName("Запретить комментарий без подтверждённого бронирования")
    void shouldNotAddCommentWithoutBooking() {
        ItemDto dto = ItemDto.builder()
                .name("Saw").description("Wood saw")
                .available(true).build();
        ItemResponse added = itemService.addItem(dto, owner.getId());

        CommentCreateDto commentReq = new CommentCreateDto();
        commentReq.setText("Nice!");

        assertThrows(
                CommentNotAllowedException.class,
                () -> commentService.addComment(added.getId(), commentReq, booker.getId()));
    }
}
