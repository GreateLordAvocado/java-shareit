package ru.practicum.shareit.gateway.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.item.dto.ItemCreateDto;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemClient client;

    @Test
    void create_should400_whenNameBlank() throws Exception {
        ItemCreateDto bad = new ItemCreateDto();
        bad.setName("   ");
        bad.setDescription("ok");
        bad.setAvailable(true);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).create(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void create_should400_whenAvailableNull() throws Exception {
        ItemCreateDto bad = new ItemCreateDto();
        bad.setName("Дрель");
        bad.setDescription("ok");
        bad.setAvailable(null);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).create(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void create_shouldDelegate_whenValid() throws Exception {
        ItemCreateDto ok = new ItemCreateDto();
        ok.setName("Дрель");
        ok.setDescription("600Вт");
        ok.setAvailable(true);

        Mockito.when(client.create(Mockito.eq(7L), ArgumentMatchers.any()))
                .thenReturn(ResponseEntity.ok("{}"));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(ok)))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).create(Mockito.eq(7L), ArgumentMatchers.any());
    }

    @Test
    void search_shouldReturnEmpty_whenTextBlank() throws Exception {
        mvc.perform(get("/items/search").param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(client, never()).search(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }
}
