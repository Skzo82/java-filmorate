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

    @Test
    void shouldAddFriendAndRemoveFriend() {
        User user1 = new User();
        user1.setEmail("a@mail.com");
        user1.setLogin("a");
        user1.setName("A");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userService.createUser(user1);

        User user2 = new User();
        user2.setEmail("b@mail.com");
        user2.setLogin("b");
        user2.setName("B");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        User created2 = userService.createUser(user2);

        userService.addFriend(created1.getId(), created2.getId());
        List<User> friends = userService.getFriends(created1.getId());
        assertEquals(1, friends.size());
        assertEquals(created2.getId(), friends.get(0).getId());

        userService.removeFriend(created1.getId(), created2.getId());
        friends = userService.getFriends(created1.getId());
        assertEquals(0, friends.size());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userService.createUser(createUser("user1", "u1@mail.com"));
        User user2 = userService.createUser(createUser("user2", "u2@mail.com"));
        User common = userService.createUser(createUser("common", "c@mail.com"));

        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(common.getId(), commonFriends.get(0).getId());
    }

    private User createUser(String name, String email) {
        User u = new User();
        u.setLogin(name);
        u.setName(name);
        u.setEmail(email);
        u.setBirthday(LocalDate.of(2000, 1, 1));
        return u;
    }


}
