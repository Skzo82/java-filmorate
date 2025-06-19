package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.time.LocalDate;

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
}
