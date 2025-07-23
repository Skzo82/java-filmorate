package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserFriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Создание двух пользователей для тестов дружбы
        user1 = new User();
        user1.setId(1);
        user1.setEmail("user1@mail.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        user2 = new User();
        user2.setId(2);
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
    }

    @Test
    void shouldAddAndRemoveFriend() throws Exception {
        // Добавление друга (user2 к user1)
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        // После добавления user2 появляется в друзьях user1
        when(userService.getFriends(1)).thenReturn(List.of(user2));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));

        // Удаление user2 из друзей user1
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        // После удаления список друзей user1 пустой
        when(userService.getFriends(1)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        // Создаем общего друга
        User common = new User();
        common.setId(3);
        common.setEmail("common@mail.com");
        common.setLogin("common");
        common.setName("Common Friend");
        common.setBirthday(LocalDate.of(1995, 5, 5));

        // Мокаем сервис, чтобы вернуть общего друга
        when(userService.getCommonFriends(1, 2)).thenReturn(List.of(common));

        // Проверяем получение списка общих друзей
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }
}
