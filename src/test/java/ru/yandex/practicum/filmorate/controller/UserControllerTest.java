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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldFailWithInvalidEmail() throws Exception {
        // Ошибка: неверный email
        String userJson = "{ \"email\": \"not-an-email\", \"login\": \"user\", \"birthday\": \"2000-01-01\" }";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateUserWithLoginAsNameIfNameMissing() throws Exception {
        // Автоматически подставляется логин как имя
        String userJson = "{\"email\": \"user@mail.com\", \"login\": \"log123\", \"birthday\": \"2000-01-01\"}";

        User expectedUser = new User();
        expectedUser.setId(1);
        expectedUser.setEmail("user@mail.com");
        expectedUser.setLogin("log123");
        expectedUser.setName("log123"); // <-- name = login
        expectedUser.setBirthday(LocalDate.of(2000, 1, 1));

        when(userService.createUser(any(User.class))).thenReturn(expectedUser);

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.name").value("log123"));
    }
    @Test
    void shouldReturn404IfUserNotFound() throws Exception {
        int nonExistentUserId = 999;

        when(userService.findById(nonExistentUserId)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }
    @Test
    void shouldReturn404OnUpdateIfUserNotFound() throws Exception {
        User userToUpdate = new User();
        userToUpdate.setId(999);
        userToUpdate.setEmail("missing@mail.com");
        userToUpdate.setLogin("missing");
        userToUpdate.setBirthday(LocalDate.of(2000, 1, 1));

        when(userService.updateUser(any(User.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }
    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        User userToUpdate = new User();
        userToUpdate.setId(1);
        userToUpdate.setEmail("update@mail.com");
        userToUpdate.setLogin("updatedLogin");
        userToUpdate.setName("Updated Name");
        userToUpdate.setBirthday(LocalDate.of(1990, 5, 15));

        when(userService.updateUser(any(User.class))).thenReturn(userToUpdate);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("update@mail.com"))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }



}
