package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void createSavesRequest() {
        UserDto requestor = userService.create(user("Requestor", "requestor@mail.test"));

        ItemRequestDto created = itemRequestService.create(requestor.getId(), request("Нужна дрель"));

        ItemRequest stored = itemRequestRepository.findById(created.getId()).orElseThrow();
        assertThat(stored.getDescription()).isEqualTo("Нужна дрель");
        assertThat(stored.getRequestor().getId()).isEqualTo(requestor.getId());
        assertThat(stored.getCreated()).isNotNull();
    }

    @Test
    void getOwnReturnsRequestsOfUser() {
        UserDto requestor = userService.create(user("Requestor", "own-requestor@mail.test"));
        UserDto other = userService.create(user("Other", "own-other@mail.test"));
        itemRequestService.create(requestor.getId(), request("Своя заявка"));
        itemRequestService.create(other.getId(), request("Чужая заявка"));

        List<ItemRequestDto> own = itemRequestService.getOwn(requestor.getId());

        assertThat(own).extracting(ItemRequestDto::getDescription).containsExactly("Своя заявка");
    }

    @Test
    void getAllReturnsOtherRequestsWithPagination() {
        UserDto requestor = userService.create(user("Requestor", "all-requestor@mail.test"));
        UserDto other = userService.create(user("Other", "all-other@mail.test"));
        itemRequestService.create(other.getId(), request("Старая заявка"));
        ItemRequestDto newer = itemRequestService.create(other.getId(), request("Новая заявка"));
        itemRequestService.create(requestor.getId(), request("Своя заявка"));

        List<ItemRequestDto> page = itemRequestService.getAll(requestor.getId(), 0, 1);

        assertThat(page).extracting(ItemRequestDto::getId).containsExactly(newer.getId());
    }

    @Test
    void getByIdReturnsRequestWithItems() {
        UserDto requestor = userService.create(user("Requestor", "byid-requestor@mail.test"));
        UserDto owner = userService.create(user("Owner", "byid-owner@mail.test"));
        ItemRequestDto created = itemRequestService.create(requestor.getId(), request("Нужна дрель"));
        itemService.create(
                owner.getId(),
                new ItemDto(null, "Дрель", "Аккумуляторная", true, created.getId(), null, null, null)
        );

        ItemRequestDto found = itemRequestService.getById(requestor.getId(), created.getId());

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(found.getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
    }

    private UserDto user(String name, String email) {
        return new UserDto(null, name, email);
    }

    private ItemRequestDto request(String description) {
        return new ItemRequestDto(null, description, null, null);
    }
}
