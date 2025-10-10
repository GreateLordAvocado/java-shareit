package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repo;
    private final UserRepository userRepo;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));
        validateForCreate(dto);

        Item toSave = ItemMapper.fromDto(dto);
        toSave.setOwnerId(ownerId);
        Item saved = repo.save(toSave);
        return ItemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        Item existing = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        if (!existing.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Редактировать вещь может только её владелец");
        }

        if (patch != null) {
            if (patch.getName() != null) {
                if (!StringUtils.hasText(patch.getName())) {
                    throw new ValidationException("Название вещи не должно быть пустым");
                }
                existing.setName(patch.getName());
            }
            if (patch.getDescription() != null) {
                if (!StringUtils.hasText(patch.getDescription())) {
                    throw new ValidationException("Описание вещи не должно быть пустым");
                }
                existing.setDescription(patch.getDescription());
            }
            if (patch.getAvailable() != null) {
                existing.setAvailable(patch.getAvailable());
            }
            if (patch.getRequestId() != null) {
                existing.setRequestId(patch.getRequestId());
            }
        }

        return ItemMapper.toDto(repo.update(existing));
    }

    @Override
    public ItemDto getById(Long itemId) {
        return repo.findById(itemId)
                .map(ItemMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));
        return repo.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return repo.searchAvailableByText(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateForCreate(ItemDto dto) {
        if (dto == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("Название вещи не должно быть пустым");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ValidationException("Описание вещи не должно быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("Поле доступности вещи (available) должно быть указано");
        }
    }
}
