package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.validation.Create;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Название не может быть пустым")
    private String name;

    @NotBlank(groups = Create.class, message = "Описание не может быть пустым")
    private String description;

    @NotNull(groups = Create.class, message = "Статус доступности обязателен")
    private Boolean available;

    private Long requestId;
}
