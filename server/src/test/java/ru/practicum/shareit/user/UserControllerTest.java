package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createReturnsSavedUser() throws Exception {
        when(userService.create(any(UserDto.class))).thenReturn(user(1L, "Ada", "ada@mail.test"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user(null, "Ada", "ada@mail.test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@mail.test"));
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(user(1L, "Ada Lovelace", "ada@mail.test"));

        mockMvc.perform(patch("/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user(null, "Ada Lovelace", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));
        verify(userService).update(eq(1L), any(UserDto.class));
    }

    @Test
    void getByIdReturnsUser() throws Exception {
        when(userService.getById(1L)).thenReturn(user(1L, "Ada", "ada@mail.test"));

        mockMvc.perform(get("/users/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada"));
    }

    @Test
    void getAllReturnsUsers() throws Exception {
        when(userService.getAll()).thenReturn(List.of(user(1L, "Ada", "ada@mail.test")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ada@mail.test"));
    }

    @Test
    void deleteCallsService() throws Exception {
        mockMvc.perform(delete("/users/{userId}", 1))
                .andExpect(status().isOk());
        verify(userService).delete(1L);
    }

    private UserDto user(Long id, String name, String email) {
        return new UserDto(id, name, email);
    }
}
