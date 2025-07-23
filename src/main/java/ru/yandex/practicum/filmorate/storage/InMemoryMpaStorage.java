package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;
import java.util.List;

public class InMemoryMpaStorage implements MpaStorage {
    @Override
    public Mpa findById(int id) {
        return new Mpa(id, "Test-MPA");
    }
    @Override
    public List<Mpa> findAll() {
        return List.of(new Mpa(1, "Test-MPA"));
    }
}
