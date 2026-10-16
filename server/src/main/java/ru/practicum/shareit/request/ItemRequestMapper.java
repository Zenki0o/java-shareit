package ru.practicum.shareit.request;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;

import java.util.ArrayList;
import java.util.List;

public class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        List<ItemRequestItemDto> itemDtos = items == null
                ? new ArrayList<>()
                : items.stream().map(ItemRequestMapper::toItemDto).toList();
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemDtos
        );
    }

    private static ItemRequestItemDto toItemDto(Item item) {
        return new ItemRequestItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                item.getOwner().getId()
        );
    }
}
