package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingRequestDto request) {
        User booker = getUserOrThrow(userId);
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + request.getItemId()));
        validateDates(request.getStart(), request.getEnd());
        if (!item.isAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Item not found: " + item.getId());
        }
        if (bookingRepository.existsOverlap(
                item.getId(), BookingStatus.APPROVED, request.getStart(), request.getEnd())) {
            throw new BadRequestException("Вещь уже забронирована на выбранные даты");
        }
        Booking booking = new Booking();
        booking.setStart(request.getStart());
        booking.setEnd(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new BadRequestException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование уже обработано");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Booking not found: " + bookingId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        getUserOrThrow(userId);
        return filter(bookingRepository.findAllByBookerId(userId), state).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, BookingState state) {
        getUserOrThrow(userId);
        return filter(bookingRepository.findAllByOwnerId(userId), state).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private List<Booking> filter(List<Booking> bookings, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> matches(booking, state, now))
                .toList();
    }

    private boolean matches(Booking booking, BookingState state, LocalDateTime now) {
        return switch (state) {
            case ALL -> true;
            case CURRENT -> !booking.getStart().isAfter(now) && !booking.getEnd().isBefore(now);
            case PAST -> booking.getEnd().isBefore(now);
            case FUTURE -> booking.getStart().isAfter(now);
            case WAITING -> booking.getStatus() == BookingStatus.WAITING;
            case REJECTED -> booking.getStatus() == BookingStatus.REJECTED;
        };
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
    }
}
