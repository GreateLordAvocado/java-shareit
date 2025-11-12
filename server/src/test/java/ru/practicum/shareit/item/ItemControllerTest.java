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
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemService itemService;

    @Test
    void create_should500_whenHeaderMissing() throws Exception {
        var dto = ItemDto.builder()
                .name("Шуруповёрт")
                .description("аккумуляторный")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        Mockito.verify(itemService, never()).create(anyLong(), any());
    }

    @Test
    void create_shouldPassThrough_whenNameBlank() throws Exception {
        var bad = ItemDto.builder()
                .name("   ")
                .description("ok")
                .available(true)
                .build();

        var returned = ItemDto.builder().id(101L).name("   ").description("ok").available(true).build();
        Mockito.when(itemService.create(eq(1L), ArgumentMatchers.any(ItemDto.class))).thenReturn(returned);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101));

        Mockito.verify(itemService, times(1)).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void create_shouldPassThrough_whenDescriptionBlank() throws Exception {
        var bad = ItemDto.builder()
                .name("Дрель")
                .description("   ")
                .available(true)
                .build();

        var returned = ItemDto.builder().id(102L).name("Дрель").description("   ").available(true).build();
        Mockito.when(itemService.create(eq(1L), ArgumentMatchers.any(ItemDto.class))).thenReturn(returned);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(102));

        Mockito.verify(itemService, times(1)).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void create_shouldPassThrough_whenAvailableNull() throws Exception {
        var bad = ItemDto.builder()
                .name("Дрель")
                .description("600Вт")
                .available(null)
                .build();

        var returned = ItemDto.builder().id(103L).name("Дрель").description("600Вт").available(null).build();
        Mockito.when(itemService.create(eq(1L), ArgumentMatchers.any(ItemDto.class))).thenReturn(returned);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(103));

        Mockito.verify(itemService, times(1)).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void create_shouldCallService_whenValid() throws Exception {
        var ok = ItemDto.builder()
                .name("Дрель")
                .description("600Вт, ударная")
                .available(true)
                .build();

        var saved = ItemDto.builder()
                .id(10L)
                .name(ok.getName())
                .description(ok.getDescription())
                .available(ok.getAvailable())
                .build();

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
        var patch = ItemDto.builder()
                .name("Новое имя")
                .build();

        Mockito.when(itemService.update(eq(5L), eq(42L), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Редактировать вещь может только её владелец"));

        mvc.perform(patch("/items/{id}", 42L)
                        .header(HDR, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("Редактировать вещь может только её владелец")));
    }

    @Test
    void patch_shouldCallService_whenValid() throws Exception {
        var patch = ItemDto.builder()
                .name("Лестница-трансформер")
                .build();

        var returned = ItemDto.builder()
                .id(3L)
                .name("Лестница-трансформер")
                .build();

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
        var i1 = ItemDto.builder().id(1L).name("Молоток").description("500 г").available(true).build();
        var i2 = ItemDto.builder().id(2L).name("Ножовка").description("по металлу").available(false).build();

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
        var i = ItemDto.builder().id(7L).name("Дрель Салют").available(true).build();

        Mockito.when(itemService.search(eq("удар"))).thenReturn(List.of(i));

        mvc.perform(get("/items/search").param("text", "удар"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Дрель Салют"));

        Mockito.verify(itemService, times(1)).search("удар");
    }
}
