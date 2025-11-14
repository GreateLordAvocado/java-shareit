package ru.practicum.shareit.gateway.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.gateway.item.dto.CommentCreateDto;
import ru.practicum.shareit.gateway.item.dto.ItemCreateDto;
import ru.practicum.shareit.gateway.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ItemClient client;

    @Nested
    class Create {
        @Test
        @DisplayName("POST /items — 400 без name/description/available (Bean Validation)")
        void create_missingFields() throws Exception {
            ItemCreateDto dto = new ItemCreateDto();
            mvc.perform(post("/items")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /items — 201 OK")
        void create_ok() throws Exception {
            ItemCreateDto dto = new ItemCreateDto();
            dto.setName("Drill");
            dto.setDescription("Bosh drill");
            dto.setAvailable(true);

            Mockito.when(client.create(anyLong(), any()))
                    .thenReturn(ResponseEntity.status(201).body("{\"id\":10}"));

            mvc.perform(post("/items")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class PatchItem {
        @Test
        @DisplayName("PATCH /items/{id} — 400 когда все поля null")
        void patch_allNull() throws Exception {
            ItemUpdateDto dto = new ItemUpdateDto();
            mvc.perform(patch("/items/{id}", 5)
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("At least one field must be provided"));
        }

        @Test
        @DisplayName("PATCH /items/{id} — 200 OK")
        void patch_ok() throws Exception {
            ItemUpdateDto dto = new ItemUpdateDto();
            dto.setDescription("upd");

            Mockito.when(client.patch(anyLong(), anyLong(), any()))
                    .thenReturn(ResponseEntity.ok("{\"id\":5}"));

            mvc.perform(patch("/items/{id}", 5)
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class GetOwnerItems {
        @Test
        @DisplayName("GET /items?from=-1 — 400 (from>=0)")
        void ownerItems_badFrom() throws Exception {
            mvc.perform(get("/items")
                            .header(HDR, 1L)
                            .param("from", "-1")
                            .param("size", "20"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("GET /items?size=0 — 400 (size>0)")
        void ownerItems_badSize() throws Exception {
            mvc.perform(get("/items")
                            .header(HDR, 1L)
                            .param("from", "0")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("GET /items — 200 OK по дефолтам from=0,size=20")
        void ownerItems_okDefault() throws Exception {
            Mockito.when(client.getOwnerItems(anyLong(), anyInt(), anyInt()))
                    .thenReturn(ResponseEntity.ok("[]"));

            mvc.perform(get("/items").header(HDR, 1L))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Search {
        @Test
        @DisplayName("GET /items/search?text=\\s — 200 и []")
        void search_emptyText() throws Exception {
            mvc.perform(get("/items/search")
                            .param("text", "   "))
                    .andExpect(status().isOk())
                    .andExpect(content().string("[]"));
        }

        @Test
        @DisplayName("GET /items/search — 200 OK")
        void search_ok() throws Exception {
            Mockito.when(client.search(any(), anyString(), anyInt(), anyInt()))
                    .thenReturn(ResponseEntity.ok("[]"));

            mvc.perform(get("/items/search")
                            .param("text", "drill")
                            .param("from", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Comments {
        @Test
        @DisplayName("POST /items/{id}/comment — 400 при пустом text")
        void comment_empty() throws Exception {
            CommentCreateDto dto = new CommentCreateDto();
            mvc.perform(post("/items/{id}/comment", 7)
                            .header(HDR, 2L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }
}
