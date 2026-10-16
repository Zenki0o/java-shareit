package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = getUserOrThrow(userId);
        ItemRequest request = new ItemRequest();
        request.setDescription(itemRequestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        getUserOrThrow(userId);
        return toDtos(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);
        if ((from != null && from < 0) || (size != null && size < 1)) {
            throw new BadRequestException("Некорректные параметры пагинации");
        }
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        if (from != null || size != null) {
            int start = from == null ? 0 : from;
            if (start >= requests.size()) {
                return List.of();
            }
            int end = size == null ? requests.size() : Math.min(start + size, requests.size());
            requests = requests.subList(start, end);
        }
        return toDtos(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        return toDtos(List.of(request)).get(0);
    }

    private List<ItemRequestDto> toDtos(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();
        Map<Long, List<Item>> itemsByRequest = requestIds.isEmpty()
                ? Map.of()
                : itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
