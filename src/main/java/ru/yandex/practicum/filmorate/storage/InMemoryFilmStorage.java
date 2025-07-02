package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger();

    @Override
    public Film createFilm(Film film) {
        film.setId(idGenerator.incrementAndGet());
        films.put(film.getId(), film);
        likes.put(film.getId(), new HashSet<>());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Фильм не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(int id) {
        return films.get(id); // Возвращает null, если фильм не найден
    }


    @Override
    public void deleteAll() {
        films.clear();
        likes.clear();
        idGenerator.set(0);
    }

    @Override
    public void addLike(int filmId, int userId) {
        if (!likes.containsKey(filmId)) {
            throw new ValidationException("Фильм не найден");
        }
        likes.get(filmId).add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        if (!likes.containsKey(filmId)) {
            throw new ValidationException("Фильм не найден");
        }
        likes.get(filmId).remove(userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(
                        likes.get(f2.getId()).size(),
                        likes.get(f1.getId()).size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
