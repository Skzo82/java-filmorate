package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryMpaStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(
                new InMemoryFilmStorage(),
                new InMemoryUserStorage(),
                new InMemoryMpaStorage(),
                new InMemoryGenreStorage()
        );
        filmService.getFilmStorage().deleteAll();

        for (int i = 1; i <= 5; i++) {
            Film film = new Film();
            film.setName("Film " + i);
            film.setDescription("Descrizione " + i);
            film.setReleaseDate(LocalDate.of(2000 + i, 1, 1));
            film.setDuration(100 + i);
            film.setMpa(new InMemoryMpaStorage().findById(1));
            filmService.createFilm(film);
        }
    }


    @Test
    void shouldCreateFilmSuccessfully() {
        Film film = createTestFilm("Test film");
        Film saved = filmService.createFilm(film);
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals("Test film", saved.getName());
    }

    @Test
    void shouldFindAllFilms() {
        filmService.getFilmStorage().deleteAll();
        filmService.createFilm(createTestFilm("F1"));
        filmService.createFilm(createTestFilm("F2"));
        List<Film> all = filmService.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldFindFilmById() {
        Film saved = filmService.createFilm(createTestFilm("Found"));
        Film found = filmService.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void shouldUpdateFilmWithPartialData() {
        Film saved = filmService.createFilm(createTestFilm("Original"));
        Film update = new Film();
        update.setId(saved.getId());
        update.setName("Updated name");
        Film result = filmService.updateFilmCustomValidation(update);
        assertEquals("Updated name", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithoutId() {
        Film update = new Film();
        update.setName("Missing ID");
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmService.updateFilmCustomValidation(update));
        assertEquals("ID фильма обязателен для обновления.", ex.getMessage());
    }

    @Test
    void shouldNotUpdateInvalidFields() {
        Film original = filmService.createFilm(createTestFilm("Valid"));
        Film update = new Film();
        update.setId(original.getId());
        update.setDescription("A".repeat(201)); // слишком длинное описание
        update.setDuration(-100);               // некорректная длительность
        update.setReleaseDate(LocalDate.of(1800, 1, 1)); // слишком старая дата
        Film result = filmService.updateFilmCustomValidation(update);
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getDuration(), result.getDuration());
        assertEquals(original.getReleaseDate(), result.getReleaseDate());
    }

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        film.setMpa(new InMemoryMpaStorage().findById(1));

        return film;
    }

    @Test
    void shouldThrowNotFoundWhenFilmDoesNotExist() {
        int missingId = 999;

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> filmService.findById(missingId));

        assertEquals("Фильм с id=" + missingId + " не найден.", ex.getMessage());
    }

}
