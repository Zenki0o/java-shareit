package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemRequestService itemRequestService;

    @Test
    void createSavesItemWithRequest() {
        UserDto owner = userService.create(user("Owner", "owner-item@mail.test"));
        ItemRequestDto request = itemRequestService.create(
                owner.getId(),
                new ItemRequestDto(null, "Нужна дрель", null, null)
        );

        ItemDto created = itemService.create(owner.getId(), item("Дрель", "Аккумуляторная", true, request.getId()));

        Item stored = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Дрель");
        assertThat(stored.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(stored.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void updateChangesStoredItem() {
        UserDto owner = userService.create(user("Owner", "owner-update@mail.test"));
        ItemDto created = itemService.create(owner.getId(), item("Дрель", "Старая", true, null));

        ItemDto updated = itemService.update(
                owner.getId(),
                created.getId(),
                item("Шуруповерт", null, false, null)
        );

        assertThat(updated.getName()).isEqualTo("Шуруповерт");
        assertThat(updated.getDescription()).isEqualTo("Старая");
        assertThat(itemRepository.findById(created.getId()).orElseThrow().isAvailable()).isFalse();
    }

    @Test
    void getByIdReturnsStoredItemForOwner() {
        UserDto owner = userService.create(user("Owner", "owner-get@mail.test"));
        ItemDto created = itemService.create(owner.getId(), item("Дрель", "Аккумуляторная", true, null));

        ItemDto found = itemService.getById(owner.getId(), created.getId());

        assertThat(found.getName()).isEqualTo("Дрель");
        assertThat(found.getComments()).isEmpty();
        assertThat(found.getLastBooking()).isNull();
    }

    @Test
    void getByOwnerReturnsItemsWithBookingsAndComments() {
        UserDto owner = userService.create(user("Owner", "owner-list@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-list@mail.test"));
        ItemDto created = itemService.create(owner.getId(), item("Дрель", "Аккумуляторная", true, null));
        LocalDateTime now = LocalDateTime.now();
        approve(booker.getId(), owner.getId(), created.getId(), now.minusDays(3), now.minusDays(1));
        approve(booker.getId(), owner.getId(), created.getId(), now.plusDays(1), now.plusDays(2));
        itemService.addComment(booker.getId(), created.getId(), new CommentDto(null, "Отличная дрель", null, null));

        List<ItemDto> items = itemService.getByOwner(owner.getId());

        assertThat(items).hasSize(1);
        ItemDto item = items.get(0);
        assertThat(item.getLastBooking().getBookerId()).isEqualTo(booker.getId());
        assertThat(item.getNextBooking().getBookerId()).isEqualTo(booker.getId());
        assertThat(item.getComments()).extracting(CommentDto::getText).containsExactly("Отличная дрель");
    }

    @Test
    void searchReadsAvailableItemsByText() {
        UserDto owner = userService.create(user("Owner", "owner-search@mail.test"));
        itemService.create(owner.getId(), item("Дрель", "Аккумуляторная", true, null));
        itemService.create(owner.getId(), item("Дрель скрытая", "Не выдается", false, null));

        List<ItemDto> found = itemService.search("дРЕЛ");

        assertThat(found).extracting(ItemDto::getName).containsExactly("Дрель");
    }

    @Test
    void addCommentSavesCommentAfterFinishedBooking() {
        UserDto owner = userService.create(user("Owner", "owner-comment@mail.test"));
        UserDto booker = userService.create(user("Booker", "booker-comment@mail.test"));
        ItemDto created = itemService.create(owner.getId(), item("Дрель", "Аккумуляторная", true, null));
        LocalDateTime now = LocalDateTime.now();
        approve(booker.getId(), owner.getId(), created.getId(), now.minusDays(3), now.minusHours(1));

        CommentDto comment = itemService.addComment(
                booker.getId(),
                created.getId(),
                new CommentDto(null, "Все хорошо", null, null)
        );

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getAuthorName()).isEqualTo("Booker");
        assertThat(itemService.getById(owner.getId(), created.getId()).getComments())
                .extracting(CommentDto::getText)
                .containsExactly("Все хорошо");
    }

    private void approve(Long bookerId, Long ownerId, Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(itemId);
        request.setStart(start);
        request.setEnd(end);
        Long bookingId = bookingService.create(bookerId, request).getId();
        assertThat(bookingService.approve(ownerId, bookingId, true).getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    private UserDto user(String name, String email) {
        return new UserDto(null, name, email);
    }

    private ItemDto item(String name, String description, Boolean available, Long requestId) {
        return new ItemDto(null, name, description, available, requestId, null, null, null);
    }
}
