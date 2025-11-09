package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserJpaRepository users;

    private static final Pattern SIMPLE_EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        validateForCreate(dto);

        String email = cleanEmail(dto.getEmail());
        String name  = dto.getName().trim();

        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email уже используется: " + email);
        }

        User toSave = User.builder()
                .name(name)
                .email(email)
                .build();

        try {
            User saved = users.save(toSave);
            return UserMapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Email уже используется: " + email);
        }
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto patch) {
        if (patch == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }

        User existing = users.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        boolean changed = false;

        if (patch.getName() != null) {
            String name = patch.getName().trim();
            if (!StringUtils.hasText(name)) {
                throw new ValidationException("Имя пользователя не должно быть пустым");
            }
            if (!name.equals(existing.getName())) {
                existing.setName(name);
                changed = true;
            }
        }

        if (patch.getEmail() != null) {
            String email = cleanEmail(patch.getEmail());
            if (!StringUtils.hasText(email) || !SIMPLE_EMAIL.matcher(email).matches()) {
                throw new ValidationException("Некорректный email");
            }
            if (!email.equalsIgnoreCase(existing.getEmail())) {
                if (users.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                    throw new ConflictException("Email уже используется: " + email);
                }
                existing.setEmail(email);
                changed = true;
            }
        }

        if (!changed) {
            return UserMapper.toDto(existing);
        }

        try {
            User saved = users.save(existing);
            return UserMapper.toDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Email уже используется: " + existing.getEmail());
        }
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
