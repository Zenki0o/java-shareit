package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItHeaders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createReturnsItem() throws Exception {
        when(itemService.create(eq(7L), any(ItemDto.class))).thenReturn(item(3L, "Дрель"));

        mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item(null, "Дрель"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void updateReturnsItem() throws Exception {
        when(itemService.update(eq(7L), eq(3L), any(ItemDto.class))).thenReturn(item(3L, "Шуруповерт"));

        mockMvc.perform(patch("/items/{itemId}", 3)
                        .header(ShareItHeaders.USER_ID, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item(null, "Шуруповерт"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Шуруповерт"));
    }

    @Test
    void getByIdReturnsItem() throws Exception {
        when(itemService.getById(7L, 3L)).thenReturn(item(3L, "Дрель"));

        mockMvc.perform(get("/items/{itemId}", 3)
                        .header(ShareItHeaders.USER_ID, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void addCommentReturnsComment() throws Exception {
        CommentDto comment = new CommentDto(9L, "Отлично", "Booker", LocalDateTime.of(2026, 5, 1, 12, 0, 0));
        when(itemService.addComment(eq(7L), eq(3L), any(CommentDto.class))).thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", 3)
                        .header(ShareItHeaders.USER_ID, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentDto(null, "Отлично", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created").value("2026-05-01T12:00:00"));
    }

    @Test
    void getByOwnerReturnsItems() throws Exception {
        when(itemService.getByOwner(7L)).thenReturn(List.of(item(3L, "Дрель")));

        mockMvc.perform(get("/items")
                        .header(ShareItHeaders.USER_ID, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
        verify(itemService).getByOwner(7L);
    }

    @Test
    void searchReturnsItems() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(item(3L, "Дрель")));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    private ItemDto item(Long id, String name) {
        return new ItemDto(id, name, "Аккумуляторная", true, null, null, null, List.of());
    }
}
