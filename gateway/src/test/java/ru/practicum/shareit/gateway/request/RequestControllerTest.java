package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean RequestClient client;

    @Nested
    class Create {
        @Test
        @DisplayName("POST /requests — 400 пустое описание")
        void create_emptyDescription() throws Exception {
            ItemRequestCreateDto dto = new ItemRequestCreateDto();
            dto.setDescription("   ");

            mvc.perform(post("/requests")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /requests — 201 OK")
        void create_ok() throws Exception {
            ItemRequestCreateDto dto = new ItemRequestCreateDto();
            dto.setDescription("Нужна дрель");

            Mockito.when(client.create(anyLong(), any()))
                    .thenReturn(ResponseEntity.status(201).body("{\"id\":99}"));

            mvc.perform(post("/requests")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /requests — 400 без заголовка")
        void create_noHeader() throws Exception {
            ItemRequestCreateDto dto = new ItemRequestCreateDto();
            dto.setDescription("нужна вещь");

            mvc.perform(post("/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    class OwnAndAll {
        @Test
        @DisplayName("GET /requests — 200 OK")
        void own_ok() throws Exception {
            Mockito.when(client.getOwn(anyLong())).thenReturn(ResponseEntity.ok("[]"));

            mvc.perform(get("/requests")
                            .header(HDR, 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /requests — 400 без заголовка")
        void own_noHeader() throws Exception {
            mvc.perform(get("/requests"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("GET /requests/all?from=-1 — 400 from>=0")
        void all_badFrom() throws Exception {
            mvc.perform(get("/requests/all")
                            .header(HDR, 1L)
                            .param("from", "-1")
                            .param("size", "20"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("GET /requests/all?size=0 — 400 size>0")
        void all_badSize() throws Exception {
            mvc.perform(get("/requests/all")
                            .header(HDR, 1L)
                            .param("from", "0")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("GET /requests/all — 200 OK с дефолтами")
        void all_ok() throws Exception {
            Mockito.when(client.getAll(anyLong(), anyInt(), anyInt()))
                    .thenReturn(ResponseEntity.ok("[]"));

            mvc.perform(get("/requests/all")
                            .header(HDR, 1L))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("GET /requests/{id} — 200 OK")
    void byId_ok() throws Exception {
        Mockito.when(client.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok("{\"id\":5}"));

        mvc.perform(get("/requests/{id}", 5L)
                        .header(HDR, 1L))
                .andExpect(status().isOk());
    }
}
