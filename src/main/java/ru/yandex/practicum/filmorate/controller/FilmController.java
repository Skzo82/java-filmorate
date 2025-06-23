package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    // Добавление фильма — с полной валидацией
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film);
        return filmService.createFilm(film);
    }

    // Обновление фильма — id обязателен, остальные поля опциональны
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        return filmService.updateFilmCustomValidation(film);
    }

    // Получение всех фильмов
    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.findAll();
    }
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException ex) {
        return ex.getMessage(); // текст ошибки как тело ответа
    }
}
