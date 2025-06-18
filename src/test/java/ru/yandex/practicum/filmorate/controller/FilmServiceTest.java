package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(new InMemoryFilmStorage());
    }

    @Test
    void shouldAddLikeAndReturnPopularFilm() {
        Film film = createTestFilm("Test");
        film = filmService.createFilm(film);
        filmService.addLike(film.getId(), 1);

        List<Film> popular = filmService.getPopularFilms(1);
        assertEquals(1, popular.size());
        assertEquals(film.getId(), popular.get(0).getId());
    }

    @Test
    void shouldRemoveLike() {
        Film film = createTestFilm("Test");
        film = filmService.createFilm(film);

        filmService.addLike(film.getId(), 1);
        filmService.removeLike(film.getId(), 1);

        List<Film> popular = filmService.getPopularFilms(1);
        assertEquals(1, popular.size());
        assertEquals(film.getId(), popular.get(0).getId());
    }

    @Test
    void shouldReturnPopularFilmsSortedByLikes() {
        Film f1 = createTestFilm("A");
        Film f2 = createTestFilm("B");
        f1 = filmService.createFilm(f1);
        f2 = filmService.createFilm(f2);

        filmService.addLike(f2.getId(), 1);
        filmService.addLike(f2.getId(), 2);
        filmService.addLike(f1.getId(), 1);

        List<Film> popular = filmService.getPopularFilms(2);
        assertEquals(f2.getId(), popular.get(0).getId());
        assertEquals(f1.getId(), popular.get(1).getId());
    }

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }
}
