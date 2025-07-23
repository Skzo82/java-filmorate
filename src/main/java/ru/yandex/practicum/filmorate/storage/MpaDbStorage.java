package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.util.List;

@Repository
@Qualifier("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Mpa findById(int id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
    }

    private final RowMapper<Mpa> mpaRowMapper = (ResultSet rs, int rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };
}
