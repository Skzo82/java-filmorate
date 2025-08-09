package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Модель фильма
 */
@Data
public class Film {

    private int id;

    @NotBlank(message = "Название не может быть пустым") // название не пустое
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов") // максимум 200 символов
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @ReleaseDateConstraint // дата не раньше 28.12.1895
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    @NotNull(message = "MPA обязателен")
    private Mpa mpa;

    private Set<Genre> genres = new LinkedHashSet<>(); // Порядок важен для сериализации

    private Set<Integer> likes = new HashSet<>();

    /**
     * Добавить лайк от пользователя.
     *
     * @param userId ID пользователя
     * @return true если лайк был добавлен, false если уже был
     */
    public boolean addLike(int userId) {
        return likes.add(userId);
    }

    /**
     * Удалить лайк пользователя.
     *
     * @param userId ID пользователя
     * @return true если лайк был удалён, false если не было
     */
    public boolean removeLike(int userId) {
        return likes.remove(userId);
    }
}
