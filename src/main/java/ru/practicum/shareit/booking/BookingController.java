package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.ShareItHeaders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                             @Valid @RequestBody BookingRequestDto request) {
        return bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                       @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getByOwner(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                              @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByBooker(@RequestHeader(ShareItHeaders.USER_ID) Long userId,
                                        @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getByBooker(userId, state);
    }
}
