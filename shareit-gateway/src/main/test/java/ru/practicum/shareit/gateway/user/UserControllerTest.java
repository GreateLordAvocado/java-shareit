package ru.practicum.shareit.gateway.user;

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
import ru.practicum.shareit.gateway.user.dto.UserDto;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserClient client;

    @Test
    void create_should400_whenEmailBad() throws Exception {
        UserDto bad = new UserDto();
        bad.setName("A");
        bad.setEmail("not-an-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).create(ArgumentMatchers.any());
    }

    @Test
    void create_shouldDelegate_whenValid() throws Exception {
        UserDto dto = new UserDto();
        dto.setName("User");
        dto.setEmail("user@example.com");

        Mockito.when(client.create(ArgumentMatchers.any()))
                .thenReturn(ResponseEntity.ok("{}"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).create(ArgumentMatchers.any());
    }

    @Test
    void patch_shouldDelegate_whenAtLeastOneFieldProvided() throws Exception {
        UserDto patch = new UserDto();
        patch.setName("New Name");

        // ВАЖНО: у клиента метод называется patch(...)
        Mockito.when(client.patch(Mockito.eq(10L), ArgumentMatchers.any()))
                .thenReturn(ResponseEntity.ok("{}"));

        mvc.perform(patch("/users/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).patch(Mockito.eq(10L), ArgumentMatchers.any());
    }
}
