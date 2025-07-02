package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public Film createFilm(Film film) {
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
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }


    // Обновление фильма с пользовательской валидацией
    public Film updateFilmCustomValidation(Film updatedFilm) {
        if (updatedFilm.getId() <= 0) {
            throw new ValidationException("ID фильма обязателен для обновления.");
        }

        Film existingFilm = findById(updatedFilm.getId());

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
}
