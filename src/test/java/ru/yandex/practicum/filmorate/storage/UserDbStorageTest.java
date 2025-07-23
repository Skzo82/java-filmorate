package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@mail.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.createUser(user1);

        user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        userStorage.createUser(user2);
    }

    @Test
    void testFindUserById() {
        User found = userStorage.findById(user1.getId());
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("user1@mail.com");
    }

    @Test
    void testUpdateUser() {
        user1.setName("Updated");
        userStorage.updateUser(user1);

        User updated = userStorage.findById(user1.getId());
        assertThat(updated.getName()).isEqualTo("Updated");
    }

    @Test
    void testFindAllUsers() {
        List<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void testAddAndRemoveFriend() {
        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());
        friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User common = new User();
        common.setEmail("common@mail.com");
        common.setLogin("common");
        common.setName("Common");
        common.setBirthday(LocalDate.of(1995, 5, 5));
        userStorage.createUser(common);

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(common.getId());
    }
}
