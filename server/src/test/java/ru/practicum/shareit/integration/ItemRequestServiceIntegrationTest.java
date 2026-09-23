package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для ItemRequestService")
@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private UserRepository userRepo;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        ownerId = userRepo.save(User.builder()
                .name("Owner").email("owner@example.com").build()).getId();
    }

    @Test
    @DisplayName("Создание запроса и его получение по id")
    void shouldCreateAndRetrieveRequest() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");

        ItemRequestResponse created = requestService.create(dto, ownerId);

        ItemRequestResponse fetched = requestService.getById(created.getId());
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    @DisplayName("Получение собственных и чужих запросов")
    void shouldReturnOwnAndOtherRequests() {

        ItemRequestDto ownerReq = new ItemRequestDto();
        ownerReq.setDescription("Owner req");
        requestService.create(ownerReq, ownerId);

        ItemRequestDto otherReq = new ItemRequestDto();
        otherReq.setDescription("Other req");

        User other = userRepo.save(User.builder()
                .name("Other").email("other@example.com")
                .build());
        requestService.create(otherReq, other.getId());

        List<ItemRequestResponse> own = requestService.getOwn(ownerId);
        assertEquals(1, own.size());

        List<ItemRequestResponse> otherRequest = requestService.getAll(ownerId);
        assertEquals(1, otherRequest.size());
    }

    @Test
    @DisplayName("Получение неизвестного запроса должно бросить NotFoundException")
    void shouldThrowNotFoundForUnknownRequest() {
        assertThrows(
                NotFoundException.class,
                () -> requestService.getById(999L));
    }

    @Test
    @DisplayName("Запрос по неизвестному ID → NotFoundException")
    void getByUnknownIdThrows() {
        assertThrows(
                NotFoundException.class,
                () -> requestService.getById(999L));
    }

    @Test
    @DisplayName("Создание запроса для несуществующего пользователя -> NotFoundException")
    void testCreateRequestNonExistentUser() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need something");

        assertThrows(NotFoundException.class,
                () -> requestService.create(dto, 999L));
    }

    @Test
    @DisplayName("Получение запроса с несуществующим ID -> NotFoundException")
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class,
                () -> requestService.getById(999L));
    }
}
