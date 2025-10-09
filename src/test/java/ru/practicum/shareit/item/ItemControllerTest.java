package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    private long createUser(String name, String email) throws Exception {
        UserDto u = new UserDto(null, name, email);
        String body = om.writeValueAsString(u);
        String json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("id").asLong();
    }

    private long createItem(long ownerId, ItemDto dto) throws Exception {
        String json = mockMvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("id").asLong();
    }

    @Test
    void createItem_ok() throws Exception {
        long ownerId = createUser("Owner", "o@ex.com");

        ItemDto dto = new ItemDto(null, "Дрель", "600Вт, ударная", true, null, null);
        mockMvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", containsString("600Вт")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createItem_missingHeader() throws Exception {
        ItemDto dto = new ItemDto(null, "Шуруповёрт", "аккумуляторный", true, null, null);
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- Разбивка бывшего createItem_validationErrors на 3 отдельных теста ---

    @Test
    void createItem_emptyName_returns400() throws Exception {
        long ownerId = createUser("Owner", "o@ex.com");

        ItemDto bad = new ItemDto(null, "  ", "ok", true, null, null);
        mockMvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Название вещи не должно быть пустым")));
    }

    @Test
    void createItem_emptyDescription_returns400() throws Exception {
        long ownerId = createUser("Owner", "o@ex.com");

        ItemDto bad = new ItemDto(null, "ok", "   ", true, null, null);
        mockMvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Описание вещи не должно быть пустым")));
    }

    @Test
    void createItem_missingAvailable_returns400() throws Exception {
        long ownerId = createUser("Owner", "o@ex.com");

        ItemDto bad = new ItemDto(null, "ok", "ok", null, null, null);
        mockMvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Поле доступности вещи")));
    }

    @Test
    void patchItem_onlyOwner_canEdit() throws Exception {
        long owner = createUser("Owner", "o@ex.com");
        long stranger = createUser("Stranger", "s@ex.com");
        long itemId = createItem(owner, new ItemDto(null, "Лестница", "3 м", true, null, null));

        ItemDto patchFromStranger = new ItemDto(null, "Чужая", null, null, null, null);
        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(HDR, stranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patchFromStranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Редактировать вещь может только её владелец")));

        ItemDto patch = new ItemDto(null, "Лестница-трансформер", null, null, null, null);
        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(HDR, owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Лестница-трансформер")));
    }

    @Test
    void getOwnerItems_ok() throws Exception {
        long owner = createUser("Owner", "o@ex.com");
        createItem(owner, new ItemDto(null, "Молоток", "500 г", true, null, null));
        createItem(owner, new ItemDto(null, "Ножовка", "по металлу", false, null, null));

        mockMvc.perform(get("/items").header(HDR, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Молоток", "Ножовка")));
    }

    @Test
    void search_items_onlyAvailable_caseInsensitive() throws Exception {
        long owner = createUser("Owner", "o@ex.com");
        createItem(owner, new ItemDto(null, "Дрель Салют", "ударная", true, null, null));
        createItem(owner, new ItemDto(null, "Перфоратор", "есть ударный режим", false, null, null)); // недоступен

        mockMvc.perform(get("/items/search").param("text", "УДАР"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Дрель Салют")))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void search_blank_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/items/search").param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
