package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserService
 */
public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    void shouldCreateUserWithNameFromLoginIfNameIsBlank() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testuser");
        user.setName("");  // пустое имя
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userService.createUser(user);

        assertEquals("testuser", created.getName(), "Имя должно быть установлено как логин");
        assertTrue(created.getId() > 0, "ID должен быть сгенерирован");
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("userlogin");
        user.setName("User");
        user.setBirthday(LocalDate.of(1995, 6, 15));

        User created = userService.createUser(user);
        created.setName("Updated");

        User updated = userService.updateUser(created);

        assertEquals("Updated", updated.getName(), "Имя должно быть обновлено");
    }

    @Test
    void shouldReturnAllUsers() {
        User u1 = new User();
        u1.setEmail("a@mail.com");
        u1.setLogin("a");
        u1.setName("A");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        userService.createUser(u1);

        User u2 = new User();
        u2.setEmail("b@mail.com");
        u2.setLogin("b");
        u2.setName("B");
        u2.setBirthday(LocalDate.of(1991, 2, 2));
        userService.createUser(u2);

        List<User> all = userService.findAll();
        assertEquals(2, all.size(), "Ожидалось 2 пользователя");
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setEmail("find@mail.com");
        user.setLogin("findme");
        user.setName("Find");
        user.setBirthday(LocalDate.of(1992, 3, 3));

        User created = userService.createUser(user);
        User found = userService.findById(created.getId());

        assertEquals(created.getEmail(), found.getEmail(), "Найденный пользователь должен совпадать");
    }

    @Test
    void shouldClearAllUsers() {
        User user = new User();
        user.setEmail("clear@mail.com");
        user.setLogin("clear");
        user.setName("Clear");
        user.setBirthday(LocalDate.of(1993, 4, 4));

        userService.createUser(user);
        userService.deleteAll();

        assertTrue(userService.findAll().isEmpty(), "Список пользователей должен быть пуст");
    }
}
