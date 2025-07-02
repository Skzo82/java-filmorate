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

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1);
        film.setName("Interstellar");
        film.setDescription("Epic science fiction film");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

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
        film.setName(""); // имя пустое
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()); // должно пройти
    }

    @Test
    void shouldRejectFilmWithTooLongDescription() throws Exception {
        Film invalidFilm = new Film();
        invalidFilm.setName("Фильм слишком длинный");
        invalidFilm.setDescription("A".repeat(201)); // строка длиной 201 символ
        invalidFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFilmWithNonPositiveDuration() throws Exception {
        // Ошибка: продолжительность фильма должна быть положительной
        Film invalidFilm = new Film();
        invalidFilm.setName("Фильм с некорректной продолжительностью");
        invalidFilm.setDescription("Описание");
        invalidFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        invalidFilm.setDuration(0); // некорректная продолжительность

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest()); // ожидается 400 Bad Request
    }

    @Test
    void shouldRejectFilmWithTooOldReleaseDate() throws Exception {
        // Ошибка: дата релиза не может быть раньше 28 декабря 1895 года
        Film invalidFilm = new Film();
        invalidFilm.setName("Старый фильм");
        invalidFilm.setDescription("Исторический фильм");
        invalidFilm.setReleaseDate(LocalDate.of(1800, 1, 1)); // слишком старая дата
        invalidFilm.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest()); // ожидается 400 Bad Request
    }

    @Test
    void shouldUpdateFilmSuccessfully() throws Exception {
        // Успешное обновление фильма
        Film updated = new Film();
        updated.setId(1);
        updated.setName("Interstellar (Extended)");
        updated.setDescription("Extended version");
        updated.setReleaseDate(LocalDate.of(2014, 11, 7));
        updated.setDuration(170);

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
        // Ошибка: отсутствует ID при обновлении
        Film invalid = new Film();
        invalid.setName("No ID Film");
        invalid.setDescription("Нет ID");
        invalid.setReleaseDate(LocalDate.of(2020, 1, 1));
        invalid.setDuration(100);

        when(filmService.updateFilmCustomValidation(any(Film.class)))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.ValidationException("ID фильма обязателен для обновления."));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest()); // ожидается 400 Bad Request
    }


    @Test
    void shouldReturn404IfFilmToUpdateNotFound() throws Exception {
        // Ошибка: фильм для обновления не найден
        Film filmToUpdate = new Film();
        filmToUpdate.setId(999);
        filmToUpdate.setName("Несуществующий фильм");
        filmToUpdate.setDescription("Фильм не найден");
        filmToUpdate.setReleaseDate(LocalDate.of(2020, 1, 1));
        filmToUpdate.setDuration(100);

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
        // Получение списка всех фильмов
        Film another = new Film();
        another.setId(2);
        another.setName("Dune");
        another.setDescription("Science fiction");
        another.setReleaseDate(LocalDate.of(2021, 10, 1));
        another.setDuration(155);

        when(filmService.findAll()).thenReturn(List.of(film, another));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

}
