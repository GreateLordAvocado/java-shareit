package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class ItemRequestServiceTest {

    @Autowired private ItemRequestService service;
    @Autowired private UserRepository userRepo;

    private Long requesterId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        User u1 = userRepo.save(new User(null, "Requester", "req@example.com"));
        User u2 = userRepo.save(new User(null, "Other", "other@example.com"));
        requesterId = u1.getId();
        otherUserId = u2.getId();
    }

    @Test
    @DisplayName("create(): успешное создание запроса")
    void create_success() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужен перфоратор");
        ItemRequestDto saved = service.create(requesterId, dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Нужен перфоратор");
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getItems()).isEmpty();
    }

    @Test
    @DisplayName("create(): пробельное описание допускается (валидация на уровне gateway), описание триммится")
    void create_allows_blank_description_and_trims() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("   ");
        ItemRequestDto saved = service.create(requesterId, dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("");
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getItems()).isEmpty();
    }

    @Test
    @DisplayName("getOwn(): сортировка по created desc и подгрузка answers")
    void getOwn_sortAndAnswers() {
        service.create(requesterId, new ItemRequestCreateDto("1"));
        service.create(requesterId, new ItemRequestCreateDto("2"));
        service.create(requesterId, new ItemRequestCreateDto("3"));

        List<ItemRequestDto> list = service.getOwn(requesterId);
        assertThat(list).hasSize(3);
        assertThat(list.get(0).getCreated()).isAfterOrEqualTo(list.get(1).getCreated());
        assertThat(list.get(1).getCreated()).isAfterOrEqualTo(list.get(2).getCreated());
    }

    @Test
    @DisplayName("getAll(): возвращает чужие запросы постранично")
    void getAll_pagination() {
        service.create(otherUserId, new ItemRequestCreateDto("A"));
        service.create(otherUserId, new ItemRequestCreateDto("B"));
        service.create(otherUserId, new ItemRequestCreateDto("C"));

        List<ItemRequestDto> page1 = service.getAll(requesterId, 0, 2);
        List<ItemRequestDto> page2 = service.getAll(requesterId, 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(1);
        assertThat(page1.get(0).getCreated()).isAfterOrEqualTo(page1.get(1).getCreated());
    }

    @Test
    @DisplayName("getById(): возвращает DTO по id, доступно любому")
    void getById_success() {
        ItemRequestDto created = service.create(otherUserId, new ItemRequestCreateDto("Нужен велосипед"));
        ItemRequestDto loaded = service.getById(requesterId, created.getId());
        assertThat(loaded.getId()).isEqualTo(created.getId());
        assertThat(loaded.getDescription()).isEqualTo("Нужен велосипед");
    }
}
