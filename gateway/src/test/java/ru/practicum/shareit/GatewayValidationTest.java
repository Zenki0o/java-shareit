package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUserWithInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"name\",\"email\":\"user.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createItemWithoutNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .header(ShareItHeaders.USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"text\",\"available\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createBookingWithEqualDatesReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2099-01-01T10:00:00\",\"end\":\"2099-01-01T10:00:00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
