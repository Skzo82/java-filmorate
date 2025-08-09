package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        if (film.getMpa() == null) {
            throw new ValidationException("MPA не может быть null");
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        film.setId((int) id);

        insertGenres(film);
        film.setGenres(findGenresByFilmId(film.getId()));

        film.setMpa(findMpaById(film.getMpa().getId()));
        return film;
    }


    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        insertGenres(film);
        film.setGenres(findGenresByFilmId(film.getId()));
        film.setMpa(findMpaById(film.getMpa().getId()));
        return film;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        for (Film film : films) {
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setMpa(findMpaById(film.getMpa().getId()));
        }
        return films;
    }

    @Override
    public Film findById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
        film.setGenres(findGenresByFilmId(film.getId()));
        film.setMpa(findMpaById(film.getMpa().getId()));
        return film;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");
    }

    // --- Gestione LIKE ---
    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.* " +
                "FROM films f LEFT JOIN film_likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        for (Film film : films) {
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setMpa(findMpaById(film.getMpa().getId()));
        }
        return films;
    }

    // --- Helpers ---

    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private Set<Genre> findGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, genreRowMapper, filmId));
    }

    private Mpa findMpaById(int mpaId) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, mpaRowMapper, mpaId);
    }

    // --- RowMappers ---

    private final RowMapper<Film> filmRowMapper = (ResultSet rs, int rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        film.setMpa(mpa);

        return film;
    };


    private final RowMapper<Genre> genreRowMapper = (ResultSet rs, int rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    };

    private final RowMapper<Mpa> mpaRowMapper = (ResultSet rs, int rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };
}
