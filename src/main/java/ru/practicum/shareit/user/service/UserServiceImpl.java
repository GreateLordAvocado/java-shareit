package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    private static final Pattern SIMPLE_EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public UserDto create(UserDto dto) {
        validateForCreate(dto);

        String normalizedEmail = normalizeEmail(dto.getEmail());
        dto.setEmail(normalizedEmail);
        dto.setName(dto.getName().trim());

        if (repo.existsByEmail(normalizedEmail, null)) {
            throw new ValidationException("Email уже используется: " + normalizedEmail);
        }

        User saved = repo.save(UserMapper.fromDto(dto));
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        if (patch == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }

        User existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        if (patch.getName() != null) {
            String name = patch.getName().trim();
            if (!StringUtils.hasText(name)) {
                throw new ValidationException("Имя пользователя не должно быть пустым");
            }
            existing.setName(name);
        }

        if (patch.getEmail() != null) {
            String email = normalizeEmail(patch.getEmail());
            if (!SIMPLE_EMAIL.matcher(email).matches()) {
                throw new ValidationException("Некорректный email");
            }
            if (repo.existsByEmail(email, id)) {
                throw new ValidationException("Email уже используется: " + email);
            }
            existing.setEmail(email);
        }

        return UserMapper.toDto(repo.update(existing));
    }

    @Override
    public UserDto getById(Long id) {
        return repo.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    @Override
    public List<UserDto> getAll() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private void validateForCreate(UserDto dto) {
        if (dto == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("Имя пользователя не должно быть пустым");
        }
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new ValidationException("Некорректный email");
        }
        String email = normalizeEmail(dto.getEmail());
        if (!SIMPLE_EMAIL.matcher(email).matches()) {
            throw new ValidationException("Некорректный email");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
