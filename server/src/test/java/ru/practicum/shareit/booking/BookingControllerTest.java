package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItHeaders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

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

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createReturnsBooking() throws Exception {
        when(bookingService.create(eq(7L), any(BookingRequestDto.class))).thenReturn(booking());

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(3L);
        request.setStart(LocalDateTime.of(2026, 6, 1, 10, 0, 0));
        request.setEnd(LocalDateTime.of(2026, 6, 2, 10, 0, 0));

        mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value("2026-06-01T10:00:00"));
    }

    @Test
    void approveReturnsBooking() throws Exception {
        when(bookingService.approve(7L, 5L, true)).thenReturn(booking());

        mockMvc.perform(patch("/bookings/{bookingId}", 5)
                        .header(ShareItHeaders.USER_ID, 7)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
        verify(bookingService).approve(7L, 5L, true);
    }

    @Test
    void getByOwnerPassesState() throws Exception {
        when(bookingService.getByOwner(7L, BookingState.FUTURE)).thenReturn(List.of(booking()));

        mockMvc.perform(get("/bookings/owner")
                        .header(ShareItHeaders.USER_ID, 7)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void getByIdReturnsBooking() throws Exception {
        when(bookingService.getById(7L, 5L)).thenReturn(booking());

        mockMvc.perform(get("/bookings/{bookingId}", 5)
                        .header(ShareItHeaders.USER_ID, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booker.id").value(8))
                .andExpect(jsonPath("$.item.id").value(3));
    }

    @Test
    void getByBookerUsesDefaultState() throws Exception {
        when(bookingService.getByBooker(7L, BookingState.ALL)).thenReturn(List.of(booking()));

        mockMvc.perform(get("/bookings")
                        .header(ShareItHeaders.USER_ID, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].end").value("2026-06-02T10:00:00"));
        verify(bookingService).getByBooker(7L, BookingState.ALL);
    }

    private BookingDto booking() {
        return new BookingDto(
                5L,
                LocalDateTime.of(2026, 6, 1, 10, 0, 0),
                LocalDateTime.of(2026, 6, 2, 10, 0, 0),
                BookingStatus.WAITING,
                new UserDto(8L, "Booker", "booker@mail.test"),
                new ItemDto(3L, "Дрель", "Аккумуляторная", true, null, null, null, null)
        );
    }
}
