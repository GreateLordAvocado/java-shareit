package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepo;
    private final ItemJpaRepository itemRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        requireUser(userId);

        String description = (dto == null) ? null : dto.getDescription();
        if (!StringUtils.hasText(description)) {
            throw new ValidationException("Описание запроса не должно быть пустым");
        }
        description = description.trim();

        ItemRequest toSave = ItemRequestMapper.toEntity(userId, description, LocalDateTime.now());
        ItemRequest saved  = requestRepo.save(toSave);

        List<Item> answers = itemRepo.findByRequestId(saved.getId());
        return ItemRequestMapper.toDto(saved, answers);
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        requireUser(userId);

        List<ItemRequest> requests = requestRepo.findByRequesterIdOrderByCreatedDesc(userId);
        if (requests.isEmpty()) return Collections.emptyList();

        Map<Long, List<Item>> answersByRequest = loadAnswersGrouped(requests);

        return requests.stream()
                .map(r -> ItemRequestMapper.toDto(
                        r,
                        answersByRequest.getOrDefault(r.getId(), Collections.<Item>emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        requireUser(userId);

        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры пагинации должны быть: from >= 0 и size > 0");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sort);

        List<ItemRequest> requests = requestRepo.findByRequesterIdNot(userId, page).getContent();
        if (requests.isEmpty()) return Collections.emptyList();

        Map<Long, List<Item>> answersByRequest = loadAnswersGrouped(requests);

        return requests.stream()
                .map(r -> ItemRequestMapper.toDto(
                        r,
                        answersByRequest.getOrDefault(r.getId(), Collections.<Item>emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        requireUser(userId);

        ItemRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден: " + requestId));

        List<Item> answers = itemRepo.findByRequestId(requestId);
        return ItemRequestMapper.toDto(request, answers);
    }

    // ===== Helpers =====

    private void requireUser(Long userId) {
        userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }

    private Map<Long, List<Item>> loadAnswersGrouped(List<ItemRequest> requests) {
        List<Long> ids = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> allAnswers = itemRepo.findByRequestIdInOrderByIdAsc(ids);

        Map<Long, List<Item>> grouped = new HashMap<>();
        for (Item i : allAnswers) {
            grouped.computeIfAbsent(i.getRequestId(), k -> new ArrayList<>()).add(i);
        }
        return grouped;
    }
}
