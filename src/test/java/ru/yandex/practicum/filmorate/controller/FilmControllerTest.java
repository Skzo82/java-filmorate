package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private FilmService filmService;

    private Film film;

    private static Film buildFilm(int id, String name, String description, LocalDate release, int duration) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(release);
        film.setDuration(duration);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        return film;
    }

    @BeforeEach
    void setUp() {
        film = buildFilm(1, "Interstellar", "Epic science fiction film", LocalDate.of(2014, 11, 7), 169);

        when(filmService.createFilm(any(Film.class))).thenReturn(film);
    }

    @Test
    void shouldCreateFilmSuccessfully() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Interstellar"));
    }

    @Test
    void shouldRejectEmptyName() throws Exception {
        Film invalidFilm = buildFilm(0, "", "desc", LocalDate.of(2020, 1, 1), 100);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFilmWithTooLongDescription() throws Exception {
        Film invalidFilm = buildFilm(0, "Фильм слишком длинный", "A".repeat(201), LocalDate.of(2020, 1, 1), 120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFilmWithNonPositiveDuration() throws Exception {
        Film invalidFilm = buildFilm(0, "Фильм с некорректной продолжительностью", "Описание", LocalDate.of(2020, 1, 1), 0);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFilmWithTooOldReleaseDate() throws Exception {
        Film invalidFilm = buildFilm(0, "Старый фильм", "Исторический фильм", LocalDate.of(1800, 1, 1), 120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFilmWithNullMpa() throws Exception {
        Film invalidFilm = buildFilm(0, "Фильм без MPA", "Описание", LocalDate.of(2020, 1, 1), 120);
        invalidFilm.setMpa(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateFilmSuccessfully() throws Exception {
        Film updated = buildFilm(1, "Interstellar (Extended)", "Extended version", LocalDate.of(2014, 11, 7), 170);

        when(filmService.updateFilmCustomValidation(any(Film.class))).thenReturn(updated);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Interstellar (Extended)"));
    }

    @Test
    void shouldFailUpdateWhenIdIsMissing() throws Exception {
        Film invalid = buildFilm(0, "No ID Film", "Нет ID", LocalDate.of(2020, 1, 1), 100);

        when(filmService.updateFilmCustomValidation(any(Film.class)))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.ValidationException("ID фильма обязателен для обновления."));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404IfFilmToUpdateNotFound() throws Exception {
        Film filmToUpdate = buildFilm(999, "Несуществующий фильм", "Фильм не найден", LocalDate.of(2020, 1, 1), 100);

        when(filmService.updateFilmCustomValidation(any(Film.class)))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Фильм не найден"));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filmToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм не найден"));
    }

    @Test
    void shouldReturnAllFilms() throws Exception {
        Film another = buildFilm(2, "Dune", "Science fiction", LocalDate.of(2021, 10, 1), 155);

        when(filmService.findAll()).thenReturn(List.of(film, another));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}
