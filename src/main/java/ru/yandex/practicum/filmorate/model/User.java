package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;

    @Email(message = "Некорректный email") // Проверка email
    @NotBlank(message = "Email не может быть пустым") // Email обязателен
    private String email;

    @NotBlank(message = "Логин не может быть пустым") // Логин обязателен
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы") // Без пробелов
    private String login;

    private String name;

    @Past(message = "Дата рождения должна быть в прошлом") // Дата в прошлом
    private LocalDate birthday;

    private Set<Integer> friends = new HashSet<>(); // Список ID друзей

    /**
     * Добавляет друга.
     *
     * @param friendId ID пользователя-друга
     * @return true если друг был добавлен, false если уже был
     */
    public boolean addFriend(int friendId) {
        return friends.add(friendId);
    }

    /**
     * Удаляет друга.
     *
     * @param friendId ID пользователя-друга
     * @return true если друг был удалён, false если его не было
     */
    public boolean removeFriend(int friendId) {
        return friends.remove(friendId);
    }


    @Override
    public String toString() {
        return "User{" + "id=" + id + ", email='" + email + '\'' + '}';
    }

}
