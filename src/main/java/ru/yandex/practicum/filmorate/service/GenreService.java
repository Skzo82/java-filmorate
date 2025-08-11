package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;

    // Получить все жанры
    public List<Genre> getAll() {
        return genreStorage.findAll();
    }

    // Получить жанр по id
    public Genre getById(int id) {
        return genreStorage.findById(id);
    }
}
