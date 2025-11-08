package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemService itemService;

    @Test
    void create_should400_whenHeaderMissing() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setName("Шуруповёрт");
        dto.setDescription("аккумуляторный");
        dto.setAvailable(true);

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).create(anyLong(), any());
    }

    @Test
    void create_should400_whenNameBlank() throws Exception {
        ItemDto bad = new ItemDto();
        bad.setName("   ");
        bad.setDescription("ok");
        bad.setAvailable(true);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).create(anyLong(), any());
    }

    @Test
    void create_should400_whenDescriptionBlank() throws Exception {
        ItemDto bad = new ItemDto();
        bad.setName("Дрель");
        bad.setDescription("   ");
        bad.setAvailable(true);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).create(anyLong(), any());
    }

    @Test
    void create_should400_whenAvailableNull() throws Exception {
        ItemDto bad = new ItemDto();
        bad.setName("Дрель");
        bad.setDescription("600Вт");
        bad.setAvailable(null);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, never()).create(anyLong(), any());
    }

    @Test
    void create_shouldCallService_whenValid() throws Exception {
        ItemDto ok = new ItemDto();
        ok.setName("Дрель");
        ok.setDescription("600Вт, ударная");
        ok.setAvailable(true);

        ItemDto saved = new ItemDto();
        saved.setId(10L);
        saved.setName(ok.getName());
        saved.setDescription(ok.getDescription());
        saved.setAvailable(ok.getAvailable());

        Mockito.when(itemService.create(eq(7L), ArgumentMatchers.any(ItemDto.class))).thenReturn(saved);

        mvc.perform(post("/items")
                        .header(HDR, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(ok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        Mockito.verify(itemService, times(1)).create(eq(7L), any(ItemDto.class));
    }

    @Test
    void patch_shouldReturn404_whenNotOwnerOrNotFound() throws Exception {
        ItemDto patch = new ItemDto();
        patch.setName("Новое имя");

        Mockito.when(itemService.update(eq(5L), eq(42L), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Редактировать вещь может только её владелец"));

        mvc.perform(patch("/items/{id}", 42L)
                        .header(HDR, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Редактировать вещь может только её владелец")));
    }

    @Test
    void patch_shouldCallService_whenValid() throws Exception {
        ItemDto patch = new ItemDto();
        patch.setName("Лестница-трансформер");

        ItemDto returned = new ItemDto();
        returned.setId(3L);
        returned.setName("Лестница-трансформер");

        Mockito.when(itemService.update(eq(1L), eq(3L), any(ItemDto.class))).thenReturn(returned);

        mvc.perform(patch("/items/{id}", 3L)
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Лестница-трансформер"));

        Mockito.verify(itemService, times(1)).update(eq(1L), eq(3L), any(ItemDto.class));
    }

    @Test
    void getOwnerItems_shouldCallService_andReturnList() throws Exception {
        ItemDto i1 = new ItemDto(); i1.setId(1L); i1.setName("Молоток");  i1.setDescription("500 г"); i1.setAvailable(true);
        ItemDto i2 = new ItemDto(); i2.setId(2L); i2.setName("Ножовка"); i2.setDescription("по металлу"); i2.setAvailable(false);

        Mockito.when(itemService.getUserItems(eq(9L))).thenReturn(List.of(i1, i2));

        mvc.perform(get("/items").header(HDR, 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Молоток", "Ножовка")));

        Mockito.verify(itemService, times(1)).getUserItems(9L);
    }

    @Test
    void search_shouldReturnEmpty_whenTextBlank() throws Exception {
        mvc.perform(get("/items/search").param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(itemService, never()).search(anyString());
    }

    @Test
    void search_shouldCallService_whenTextProvided() throws Exception {
        ItemDto i = new ItemDto(); i.setId(7L); i.setName("Дрель Салют"); i.setAvailable(true);

        Mockito.when(itemService.search(eq("удар"))).thenReturn(List.of(i));

        mvc.perform(get("/items/search").param("text", "удар"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Дрель Салют"));

        Mockito.verify(itemService, times(1)).search("удар");
    }
}
