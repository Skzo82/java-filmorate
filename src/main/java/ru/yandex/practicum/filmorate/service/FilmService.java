package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

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

    // Добавить лайк фильму
    public void addLike(int filmId, int userId) {
        Film film = findById(filmId); // 404 если нет фильма
        userStorage.findById(userId); // выбросит исключение если не найден
        film.addLike(userId);
        // Если есть хранение лайков в БД, тут надо добавить/сохранить лайк
    }

    // Удалить лайк у фильма
    public void removeLike(int filmId, int userId) {
        Film film = findById(filmId); // 404 если нет фильма
        userStorage.findById(userId); // выбросит исключение если не найден
        film.removeLike(userId);
        // Если есть хранение лайков в БД, тут надо удалить лайк
    }

    // Получить популярные фильмы
    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
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

    public Film getFilmById(int id) {
        return findById(id);
    }

}
