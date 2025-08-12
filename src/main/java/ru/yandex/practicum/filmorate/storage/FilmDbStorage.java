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
        final String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapperWithMpa);
        attachGenres(films);
        return films;
    }

    @Override
    public Film findById(int id) {
        final String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                WHERE f.id = ?
                """;
        Film film = jdbcTemplate.queryForObject(sql, filmRowMapperWithMpa, id);
        film.setGenres(findGenresByFilmIds(List.of(film.getId()))
                .getOrDefault(film.getId(), new LinkedHashSet<>()));
        return film;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");
    }

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
        final String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name,
                       COALESCE(COUNT(l.user_id), 0) AS likes_cnt
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                LEFT JOIN film_likes l ON l.film_id = f.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, m.id, m.name
                ORDER BY likes_cnt DESC, f.id
                LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapperWithMpa, count);
        attachGenres(films);
        return films;
    }

    // --- Helpers ---

    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        LinkedHashSet<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        final String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql,
                genreIds.stream().map(id -> new Object[]{film.getId(), id}).toList());
    }

    private Set<Genre> findGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, genreRowMapper, filmId));
    }

    private void attachGenres(List<Film> films) {
        if (films.isEmpty()) return;
        Map<Integer, Set<Genre>> byFilm = findGenresByFilmIds(
                films.stream().map(Film::getId).toList()
        );
        for (Film f : films) {
            f.setGenres(byFilm.getOrDefault(f.getId(), new LinkedHashSet<>()));
        }
    }

    private Map<Integer, Set<Genre>> findGenresByFilmIds(List<Integer> filmIds) {
        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        final String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id IN (%s)
                ORDER BY g.id
                """.formatted(inSql);

        Map<Integer, Set<Genre>> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Genre g = new Genre();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            map.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(g);
        }, filmIds.toArray());
        return map;
    }

    private Mpa findMpaById(int mpaId) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, mpaRowMapper, mpaId);
    }

    // --- RowMappers ---

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

    private final RowMapper<Film> filmRowMapperWithMpa = (ResultSet rs, int rn) -> {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        f.setReleaseDate(rs.getDate("release_date").toLocalDate());
        f.setDuration(rs.getInt("duration"));
        Mpa m = new Mpa();
        m.setId(rs.getInt("mpa_id"));
        m.setName(rs.getString("mpa_name"));
        f.setMpa(m);
        return f;
    };
}
