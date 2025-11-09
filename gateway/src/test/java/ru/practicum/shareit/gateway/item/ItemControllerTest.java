package ru.practicum.shareit.gateway.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired private MockMvc mvc;

    @MockBean private ItemClient client;

    @Test
    @DisplayName("POST /items: без заголовка — 400")
    void create_missingHeader() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"description\":\"y\",\"available\":true}"))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).create(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("GET /items/search: пустой text — [] и 200 (клиент не вызывается)")
    void search_emptyText() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", " "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(client, never()).search(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    @DisplayName("POST /items: корректный запрос с requestId — 200 и делегирование в клиент")
    void create_with_requestId_ok() throws Exception {
        Mockito.when(client.create(Mockito.eq(1L), Mockito.any()))
                .thenReturn(ResponseEntity.ok("{\"id\":1}"));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"description\":\"y\",\"available\":true,\"requestId\":5}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1}"));

        Mockito.verify(client, times(1)).create(Mockito.eq(1L), Mockito.any());
    }
}
