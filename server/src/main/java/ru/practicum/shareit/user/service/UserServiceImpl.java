package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserJpaRepository users;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        User toSave = User.builder()
                .name(dto.getName() == null ? null : dto.getName().trim())
                .email(dto.getEmail() == null ? null : dto.getEmail().trim())
                .build();

        User saved = users.save(toSave);
        return UserMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto patch) {
        User existing = users.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        boolean changed = false;

        if (patch.getName() != null) {
            String name = patch.getName() == null ? null : patch.getName().trim();
            if (!equalsNullable(existing.getName(), name)) {
                existing.setName(name);
                changed = true;
            }
        }

        if (patch.getEmail() != null) {
            String email = patch.getEmail() == null ? null : patch.getEmail().trim();
            if (!equalsNullableIgnoreCase(existing.getEmail(), email)) {
                existing.setEmail(email);
                changed = true;
            }
        }

        if (!changed) {
            return UserMapper.toDto(existing);
        }

        User saved = users.save(existing);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto getById(Long id) {
        return users.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    @Override
    public List<UserDto> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return users.findAll(sort).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        users.deleteById(id);
    }

    @Override
    public User requireEntity(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    private static boolean equalsNullable(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    private static boolean equalsNullableIgnoreCase(String a, String b) {
        if (a == null) return b == null;
        return a.equalsIgnoreCase(b);
    }
}
