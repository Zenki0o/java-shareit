package ru.practicum.shareit.request.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private Long ownerId;
}
