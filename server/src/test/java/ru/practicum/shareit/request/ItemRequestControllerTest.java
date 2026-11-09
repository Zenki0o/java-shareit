package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItHeaders;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createReturnsRequest() throws Exception {
        when(itemRequestService.create(eq(7L), any(ItemRequestDto.class))).thenReturn(request());

        mockMvc.perform(post("/requests")
                        .header(ShareItHeaders.USER_ID, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequestDto(null, "Нужна дрель", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.created").value("2026-04-01T09:30:00"));
    }

    @Test
    void getOwnReturnsRequests() throws Exception {
        when(itemRequestService.getOwn(7L)).thenReturn(List.of(request()));

        mockMvc.perform(get("/requests")
                        .header(ShareItHeaders.USER_ID, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getAllPassesPagination() throws Exception {
        when(itemRequestService.getAll(7L, 0, 10)).thenReturn(List.of(request()));

        mockMvc.perform(get("/requests/all")
                        .header(ShareItHeaders.USER_ID, 7)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
        verify(itemRequestService).getAll(7L, 0, 10);
    }

    @Test
    void getByIdReturnsRequestWithItems() throws Exception {
        when(itemRequestService.getById(7L, 4L)).thenReturn(request());

        mockMvc.perform(get("/requests/{requestId}", 4)
                        .header(ShareItHeaders.USER_ID, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Дрель"))
                .andExpect(jsonPath("$.items[0].ownerId").value(2));
    }

    private ItemRequestDto request() {
        return new ItemRequestDto(
                4L,
                "Нужна дрель",
                LocalDateTime.of(2026, 4, 1, 9, 30, 0),
                List.of(new ItemRequestItemDto(3L, "Дрель", "Аккумуляторная", true, 4L, 2L))
        );
    }
}
