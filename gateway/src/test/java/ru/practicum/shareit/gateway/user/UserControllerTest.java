package ru.practicum.shareit.gateway.user;

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
import ru.practicum.shareit.gateway.user.dto.UserCreateDto;
import ru.practicum.shareit.gateway.user.dto.UserUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserClient client;

    @Nested
    class Create {
        @Test
        @DisplayName("POST /users — 400 без email")
        void create_noEmail() throws Exception {
            UserCreateDto dto = new UserCreateDto();
            dto.setName("Ann");
            mvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /users — 400 невалидный email")
        void create_badEmail() throws Exception {
            UserCreateDto dto = new UserCreateDto();
            dto.setName("Ann");
            dto.setEmail("bad@@mail");
            mvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /users — 201 OK")
        void create_ok() throws Exception {
            UserCreateDto dto = new UserCreateDto();
            dto.setName("Ann");
            dto.setEmail("a@b.com");

            Mockito.when(client.create(any()))
                    .thenReturn(ResponseEntity.status(201).body("{\"id\":1}"));

            mvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class PatchUser {
        @Test
        @DisplayName("PATCH /users/{id} — 400 когда имя и email оба null")
        void patch_allNull() throws Exception {
            UserUpdateDto dto = new UserUpdateDto();
            mvc.perform(patch("/users/{id}", 3L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Body must contain at least one of the fields: name or email"));
        }

        @Test
        @DisplayName("PATCH /users/{id} — 200 OK")
        void patch_ok() throws Exception {
            UserUpdateDto dto = new UserUpdateDto();
            dto.setName("New");

            Mockito.when(client.patch(anyLong(), any()))
                    .thenReturn(ResponseEntity.ok("{\"id\":3}"));

            mvc.perform(patch("/users/{id}", 3L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("GET /users — 200 OK")
    void getAll_ok() throws Exception {
        Mockito.when(client.getAll()).thenReturn(ResponseEntity.ok("[]"));
        mvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users/{id} — 200 OK")
    void getById_ok() throws Exception {
        Mockito.when(client.getById(anyLong())).thenReturn(ResponseEntity.ok("{\"id\":5}"));
        mvc.perform(get("/users/{id}", 5L)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /users/{id} — 204/200 OK")
    void delete_ok() throws Exception {
        Mockito.when(client.delete(anyLong())).thenReturn(ResponseEntity.noContent().build());
        mvc.perform(delete("/users/{id}", 5L)).andExpect(status().isNoContent());
    }
}
