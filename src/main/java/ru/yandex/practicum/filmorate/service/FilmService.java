package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }
    public FilmStorage getFilmStorage() {
        return filmStorage;
    }

    public Film createFilm(Film film) {

        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new ValidationException("MPA obbligatorio");
        }

        mpaStorage.findById(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId());
            }
        }

        return filmStorage.createFilm(film);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        Film film = filmStorage.findById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден.");
        }
        return film;
    }


    public void addLike(int filmId, int userId) {
        Film film = findById(filmId);
        userStorage.findById(userId);
        film.addLike(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = findById(filmId);
        userStorage.findById(userId);
        film.removeLike(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film updateFilmCustomValidation(Film updatedFilm) {
        if (updatedFilm.getId() <= 0) {
            throw new ValidationException("ID фильма обязателен для обновления.");
        }

        // Валидация существования фильма
        Film existingFilm = findById(updatedFilm.getId());

        // Валидация и обновление MPA, если изменён
        if (updatedFilm.getMpa() != null && updatedFilm.getMpa().getId() != 0) {
            mpaStorage.findById(updatedFilm.getMpa().getId()); // NotFoundException если не найден
            existingFilm.setMpa(updatedFilm.getMpa());
        }

        // Валидация и обновление жанров, если изменены
        if (updatedFilm.getGenres() != null) {
            for (Genre genre : updatedFilm.getGenres()) {
                genreStorage.findById(genre.getId());
            }
            existingFilm.setGenres(updatedFilm.getGenres());
        }

        if (updatedFilm.getName() != null && !updatedFilm.getName().isBlank()) {
            existingFilm.setName(updatedFilm.getName());
        }
        if (updatedFilm.getDescription() != null && updatedFilm.getDescription().length() <= 200) {
            existingFilm.setDescription(updatedFilm.getDescription());
        }
        if (updatedFilm.getReleaseDate() != null && !updatedFilm.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            existingFilm.setReleaseDate(updatedFilm.getReleaseDate());
        }
        if (updatedFilm.getDuration() > 0) {
            existingFilm.setDuration(updatedFilm.getDuration());
        }

        return filmStorage.updateFilm(existingFilm);
    }


    public Film getFilmById(int id) {
        return findById(id);
    }
}
