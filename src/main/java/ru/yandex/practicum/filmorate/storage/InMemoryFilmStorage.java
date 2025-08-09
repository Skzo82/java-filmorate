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
    private final AtomicInteger idGenerator = new AtomicInteger();

    @Override
    public Film createFilm(Film film) {
        film.setId(idGenerator.incrementAndGet());
        films.put(film.getId(), film);
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
        return films.get(id);
    }

    @Override
    public void deleteAll() {
        films.clear();
        idGenerator.set(0);
    }

    @Override
    public void addLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new ValidationException("Фильм не найден");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new ValidationException("Фильм не найден");
        }
        if (film.getLikes() != null) {
            film.getLikes().remove(userId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt(f -> -(f.getLikes() == null ? 0 : f.getLikes().size())))
                .limit(count)
                .collect(Collectors.toList());
    }
}
