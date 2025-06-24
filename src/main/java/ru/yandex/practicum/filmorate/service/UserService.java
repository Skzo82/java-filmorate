package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(int id) {
        return userStorage.findById(id);
    }

    public void deleteAll() {
        userStorage.deleteAll();
    }

    public void addFriend(int id, int friendId) {
        User user = findById(id);
        User friend = findById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    public void removeFriend(int id, int friendId) {
        User user = findById(id);
        User friend = findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    public List<User> getFriends(int id) {
        User user = findById(id);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        User user1 = findById(id);
        User user2 = findById(otherId);

        Set<Integer> commonIds = new HashSet<>(user1.getFriends());
        commonIds.retainAll(user2.getFriends());

        return commonIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public User updateUserCustomValidation(User updatedUser) {
        if (updatedUser.getId() <= 0) {
            throw new ValidationException("ID пользователя обязателен для обновления.");
        }

        User existing = findById(updatedUser.getId());

        if (updatedUser.getEmail() != null) {
            existing.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getLogin() != null) {
            existing.setLogin(updatedUser.getLogin());
        }
        if (updatedUser.getName() != null) {
            existing.setName(updatedUser.getName());
        }
        if (updatedUser.getBirthday() != null) {
            existing.setBirthday(updatedUser.getBirthday());
        }

        return existing;
    }

}
