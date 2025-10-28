package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserJpaRepository users;

    private static final Pattern SIMPLE_EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public UserDto create(UserDto dto) {
        validateForCreate(dto);

        String email = cleanEmail(dto.getEmail());
        String name  = dto.getName().trim();

        if (users.existsByEmailCaseInsensitive(email, null)) {
            throw new ConflictException("Email уже используется: " + email);
        }

        User toSave = User.builder()
                .name(name)
                .email(email)
                .build();

        User saved = users.save(toSave);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        if (patch == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }

        User existing = users.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        if (patch.getName() != null) {
            String name = patch.getName().trim();
            if (!StringUtils.hasText(name)) {
                throw new ValidationException("Имя пользователя не должно быть пустым");
            }
            existing.setName(name);
        }

        if (patch.getEmail() != null) {
            String email = cleanEmail(patch.getEmail());
            if (!SIMPLE_EMAIL.matcher(email).matches()) {
                throw new ValidationException("Некорректный email");
            }
            if (users.existsByEmailCaseInsensitive(email, id)) {
                throw new ConflictException("Email уже используется: " + email);
            }
            existing.setEmail(email);
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
    public void delete(Long id) {
        users.deleteById(id);
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
        String email = cleanEmail(dto.getEmail());
        if (!SIMPLE_EMAIL.matcher(email).matches()) {
            throw new ValidationException("Некорректный email");
        }
    }

    private String cleanEmail(String email) {
        return email == null ? null : email.trim();
    }
}
