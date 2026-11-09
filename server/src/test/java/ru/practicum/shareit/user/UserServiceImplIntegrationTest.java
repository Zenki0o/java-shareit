package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createSavesUser() {
        UserDto created = userService.create(user("Ada", "ada@mail.test"));

        assertThat(userRepository.findById(created.getId()))
                .get()
                .extracting(User::getName, User::getEmail)
                .containsExactly("Ada", "ada@mail.test");
    }

    @Test
    void updateChangesStoredUser() {
        UserDto created = userService.create(user("Ada", "ada@mail.test"));

        UserDto updated = userService.update(created.getId(), user("Ada Lovelace", "ada.lovelace@mail.test"));

        assertThat(updated.getName()).isEqualTo("Ada Lovelace");
        assertThat(userRepository.findById(created.getId()))
                .get()
                .extracting(User::getEmail)
                .isEqualTo("ada.lovelace@mail.test");
    }

    @Test
    void getByIdReadsUserFromDatabase() {
        UserDto created = userService.create(user("Ada", "ada@mail.test"));

        UserDto found = userService.getById(created.getId());

        assertThat(found.getEmail()).isEqualTo("ada@mail.test");
    }

    @Test
    void getAllReturnsStoredUsers() {
        userService.create(user("Ada", "ada@mail.test"));
        userService.create(user("Alan", "alan@mail.test"));

        assertThat(userService.getAll())
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("ada@mail.test", "alan@mail.test");
    }

    @Test
    void deleteRemovesUser() {
        UserDto created = userService.create(user("Ada", "ada@mail.test"));

        userService.delete(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }

    private UserDto user(String name, String email) {
        return new UserDto(null, name, email);
    }
}
