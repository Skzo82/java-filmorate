package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    @Qualifier("mpaDbStorage")
    private final MpaStorage mpaStorage;

    // Получить все рейтинги
    public List<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    // Получить рейтинг по id
    public Mpa getById(int id) {
        return mpaStorage.findById(id);
    }
}
