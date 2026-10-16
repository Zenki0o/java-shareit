package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void createSavesWaitingBooking() {
        UserDto owner = userService.create(user("Owner", "owner-booking@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-booking@mail.test"));
        ItemDto item = itemService.create(owner.getId(), item());

        BookingDto created = bookingService.create(booker.getId(), request(item.getId(), 1, 2));

        Booking stored = bookingRepository.findById(created.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(stored.getItem().getId()).isEqualTo(item.getId());
        assertThat(stored.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void approveUpdatesStoredStatus() {
        UserDto owner = userService.create(user("Owner", "owner-approve@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-approve@mail.test"));
        ItemDto item = itemService.create(owner.getId(), item());
        BookingDto created = bookingService.create(booker.getId(), request(item.getId(), 1, 2));

        BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingRepository.findById(created.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getByIdReadsBookingForBooker() {
        UserDto owner = userService.create(user("Owner", "owner-get-booking@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-get-booking@mail.test"));
        ItemDto item = itemService.create(owner.getId(), item());
        BookingDto created = bookingService.create(booker.getId(), request(item.getId(), 1, 2));

        BookingDto found = bookingService.getById(booker.getId(), created.getId());

        assertThat(found.getItem().getId()).isEqualTo(item.getId());
        assertThat(found.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getByBookerReturnsFutureBookings() {
        UserDto owner = userService.create(user("Owner", "owner-booker-list@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-list-booking@mail.test"));
        ItemDto item = itemService.create(owner.getId(), item());
        bookingService.create(booker.getId(), request(item.getId(), 2, 3));

        List<BookingDto> bookings = bookingService.getByBooker(booker.getId(), BookingState.FUTURE);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getByOwnerReturnsBookingsOfOwnedItems() {
        UserDto owner = userService.create(user("Owner", "owner-list-booking@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-owner-list@mail.test"));
        ItemDto item = itemService.create(owner.getId(), item());
        BookingDto created = bookingService.create(booker.getId(), request(item.getId(), 2, 3));

        List<BookingDto> bookings = bookingService.getByOwner(owner.getId(), BookingState.ALL);

        assertThat(bookings).extracting(BookingDto::getId).containsExactly(created.getId());
    }

    private BookingRequestDto request(Long itemId, int startDays, int endDays) {
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().plusDays(startDays));
        request.setEnd(LocalDateTime.now().plusDays(endDays));
        return request;
    }

    private UserDto user(String name, String email) {
        return new UserDto(null, name, email);
    }

    private ItemDto item() {
        return new ItemDto(null, "Дрель", "Аккумуляторная", true, null, null, null, null);
    }
}
