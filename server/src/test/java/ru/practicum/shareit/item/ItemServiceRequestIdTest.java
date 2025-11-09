package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class ItemServiceRequestIdTest {

    @Autowired private ItemService itemService;
    @Autowired private ItemRequestService requestService;
    @Autowired private UserRepository userRepo;

    private Long ownerId;

    @BeforeEach
    void setup() {
        ownerId = userRepo.save(new User(null, "Owner", "owner@example.com")).getId();
        userRepo.save(new User(null, "Requester", "req@example.com"));
    }

    @Test
    @DisplayName("create(): без requestId — успешно")
    void create_without_requestId() {
        ItemDto dto = ItemDto.builder()
                .name("Дрель").description("Ударная").available(true)
                .build();
        ItemDto saved = itemService.create(ownerId, dto);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRequestId()).isNull();
    }

    @Test
    @DisplayName("create(): с валидным requestId — успешно")
    void create_with_valid_requestId() {
        Long requesterId = userRepo.findAll().stream()
                .filter(u -> !u.getId().equals(ownerId))
                .findFirst().get().getId();

        ItemRequestDto req = requestService.create(requesterId, new ItemRequestCreateDto("Нужна дрель"));

        ItemDto dto = ItemDto.builder()
                .name("Дрель Makita").description("сф").available(true)
                .requestId(req.getId())
                .build();

        ItemDto saved = itemService.create(ownerId, dto);
        assertThat(saved.getRequestId()).isEqualTo(req.getId());
    }

    @Test
    @DisplayName("create(): с несуществующим requestId — 404")
    void create_with_invalid_requestId() {
        ItemDto dto = ItemDto.builder()
                .name("Отвёртка").description("крестовая").available(true)
                .requestId(9999L)
                .build();
        assertThrows(RuntimeException.class, () -> itemService.create(ownerId, dto));
    }
}
