package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {

    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;

    // Получение списка всех жанров
    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Запрошен список всех жанров");
        return genreStorage.findAll();
    }

    // Получение жанра по id
    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.info("Запрошен жанр с id: {}", id);
        return genreStorage.findById(id);
    }
}
