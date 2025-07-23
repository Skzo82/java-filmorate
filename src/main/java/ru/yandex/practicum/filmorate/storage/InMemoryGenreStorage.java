package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;

public class InMemoryGenreStorage implements GenreStorage {
    @Override
    public Genre findById(int id) {
        return new Genre(id, "Test-Genre");
    }
    @Override
    public List<Genre> findAll() {
        return List.of(new Genre(1, "Test-Genre"));
    }
}
