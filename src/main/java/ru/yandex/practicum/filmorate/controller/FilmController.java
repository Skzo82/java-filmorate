package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

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
    public Film updateFilm(@RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        return filmService.updateFilmCustomValidation(film);
    }

    // Получение всех фильмов
    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.findAll();
    }

    // Поставить лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId); // Le eccezioni vengono gestite dal GlobalExceptionHandler
        return ResponseEntity.ok().build();
    }

    // Удалить лайк у фильма
    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    // Получить топ популярных фильмов
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
    // Получение фильма по id
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }
}
