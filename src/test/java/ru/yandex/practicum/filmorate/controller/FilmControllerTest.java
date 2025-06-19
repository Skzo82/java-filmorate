package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
    void shouldReturnFilmsInPopularityOrder() throws Exception {
        Film secondFilm = new Film();
        secondFilm.setId(2);
        secondFilm.setName("Inception");
        secondFilm.setDescription("Mind-bending thriller");
        secondFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        secondFilm.setDuration(148);

        when(filmService.getPopularFilms(2)).thenReturn(List.of(secondFilm, film));

        mockMvc.perform(get("/films/popular?count=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));
    }
}
