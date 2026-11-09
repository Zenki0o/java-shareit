package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
    @NotBlank(message = "Текст отзыва не может быть пустым")
    @Size(max = 1024, message = "Текст отзыва не может быть длиннее 1024 символов")
    private String text;
}
