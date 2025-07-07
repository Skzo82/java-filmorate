package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для управления друзьями пользователей
 */
@WebMvcTest(UserController.class)
public class UserFriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldAddAndRemoveFriendsCorrectly() throws Exception {
        User user1 = new User();
        user1.setId(1);
        user1.setEmail("user1@mail.com");
        user1.setLogin("user1");
        user1.setName("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setId(2);
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setName("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        when(userService.createUser(any())).thenReturn(user1).thenReturn(user2);
        when(userService.getFriends(1)).thenReturn(List.of(user2)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user2)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldShowCommonFriends() throws Exception {
        User commonFriend = new User();
        commonFriend.setId(3);
        commonFriend.setEmail("c@mail.com");
        commonFriend.setLogin("c");
        commonFriend.setName("c");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));

        when(userService.getCommonFriends(1, 2)).thenReturn(List.of(commonFriend));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }
}
