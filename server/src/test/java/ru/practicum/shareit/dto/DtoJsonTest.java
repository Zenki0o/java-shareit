package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonTest {
    @Autowired
    private JacksonTester<BookingRequestDto> bookingRequestJson;

    @Autowired
    private JacksonTester<BookingDto> bookingJson;

    @Autowired
    private JacksonTester<CommentDto> commentJson;

    @Autowired
    private JacksonTester<ItemBookingDto> itemBookingJson;

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestJson;

    @Test
    void bookingRequestUsesSecondPrecision() throws Exception {
        String content = "{\"itemId\":3,\"start\":\"2026-06-01T10:15:30\",\"end\":\"2026-06-02T11:00:00\"}";

        BookingRequestDto dto = bookingRequestJson.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(3L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 6, 1, 10, 15, 30));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 6, 2, 11, 0, 0));
        assertThat(bookingRequestJson.write(dto))
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-06-01T10:15:30");
        assertThat(bookingRequestJson.write(dto))
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-06-02T11:00:00");
    }

    @Test
    void bookingWritesDatesWithoutFraction() throws Exception {
        BookingDto dto = new BookingDto(
                5L,
                LocalDateTime.of(2026, 6, 1, 10, 0, 0),
                LocalDateTime.of(2026, 6, 2, 10, 0, 0),
                BookingStatus.APPROVED,
                new UserDto(8L, "Booker", "booker@mail.test"),
                new ItemDto(3L, "Дрель", "Аккумуляторная", true, null, null, null, null)
        );

        assertThat(bookingJson.write(dto))
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-06-01T10:00:00");
        assertThat(bookingJson.write(dto))
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2026-06-02T10:00:00");
        assertThat(bookingJson.write(dto))
                .extractingJsonPathStringValue("$.status")
                .isEqualTo("APPROVED");
    }

    @Test
    void commentReadsAndWritesCreated() throws Exception {
        String content = "{\"id\":9,\"text\":\"Отлично\",\"authorName\":\"Booker\",\"created\":\"2026-05-01T12:00:00\"}";

        CommentDto dto = commentJson.parseObject(content);

        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 0, 0));
        assertThat(commentJson.write(dto))
                .extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-05-01T12:00:00");
    }

    @Test
    void itemBookingUsesSecondPrecision() throws Exception {
        ItemBookingDto dto = new ItemBookingDto(
                5L,
                8L,
                LocalDateTime.of(2026, 6, 1, 10, 0, 0),
                LocalDateTime.of(2026, 6, 2, 10, 0, 0)
        );

        assertThat(itemBookingJson.write(dto))
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-06-01T10:00:00");
        ItemBookingDto parsed = itemBookingJson.parseObject(
                "{\"id\":5,\"bookerId\":8,\"start\":\"2026-06-01T10:00:00\",\"end\":\"2026-06-02T10:00:00\"}"
        );
        assertThat(parsed.getEnd()).isEqualTo(LocalDateTime.of(2026, 6, 2, 10, 0, 0));
        assertThat(parsed.getBookerId()).isEqualTo(8L);
    }

    @Test
    void itemRequestKeepsEmptyItemsAndFormatsCreated() throws Exception {
        ItemRequestDto parsed = itemRequestJson.parseObject(
                "{\"id\":4,\"description\":\"Нужна дрель\",\"created\":\"2026-04-01T09:30:00\"}"
        );

        assertThat(parsed.getCreated()).isEqualTo(LocalDateTime.of(2026, 4, 1, 9, 30, 0));
        assertThat(parsed.getItems()).isEmpty();
        assertThat(itemRequestJson.write(parsed))
                .extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-04-01T09:30:00");
    }
}
