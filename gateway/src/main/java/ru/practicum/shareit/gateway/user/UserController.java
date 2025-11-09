package ru.practicum.shareit.gateway.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.gateway.user.dto.UserCreateDto;
import ru.practicum.shareit.gateway.user.dto.UserUpdateDto;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserClient client;

    public UserController(UserClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody UserCreateDto dto) {
        return client.create(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @Valid @RequestBody UserUpdateDto dto) {
        // Частичное обновление: хотя бы одно поле должно быть задано
        if (dto.getName() == null && dto.getEmail() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Body must contain at least one of the fields: name or email"
            );
        }
        return client.patch(id, dto);
    }

    @GetMapping
    public ResponseEntity<String> getAll() {
        return client.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable Long id) {
        return client.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return client.delete(id);
    }
}
