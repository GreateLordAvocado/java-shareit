package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public UserDto create(@RequestBody UserDto dto) {
        log.debug("POST /users body={}", dto);
        return service.create(dto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto patch) {
        log.debug("PATCH /users/{} body={}", id, patch);
        return service.update(id, patch);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        log.debug("GET /users/{}", id);
        return service.getById(id);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.debug("GET /users");
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.debug("DELETE /users/{}", id);
        service.delete(id);
    }
}
