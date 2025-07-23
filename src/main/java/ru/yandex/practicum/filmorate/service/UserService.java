package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        User user = userStorage.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    public void deleteAll() {
        userStorage.deleteAll();
    }

    // Добавление друга (реализация через storage — односторонняя дружба)
    public void addFriend(int id, int friendId) {
        findById(id);       // Проверяем, что оба пользователя существуют
        findById(friendId);
        userStorage.addFriend(id, friendId);
    }

    // Удаление из друзей (storage)
    public void removeFriend(int id, int friendId) {
        findById(id);
        findById(friendId);
        userStorage.removeFriend(id, friendId);
    }

    // Получить список друзей пользователя (storage)
    public List<User> getFriends(int id) {
        findById(id);
        return userStorage.getFriends(id);
    }

    // Получить общих друзей двух пользователей (storage)
    public List<User> getCommonFriends(int id, int otherId) {
        findById(id);
        findById(otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    // Кастомная валидация для обновления пользователя
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

        return userStorage.updateUser(existing);
    }
}
