package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    @Qualifier("mpaDbStorage")
    private final MpaStorage mpaStorage;

    // Получение списка всех рейтингов MPA
    @GetMapping
    public List<Mpa> getAllMpa() {
        log.info("Запрошен список всех рейтингов MPA");
        return mpaStorage.findAll();
    }

    // Получение рейтинга по id
    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        log.info("Запрошен рейтинг MPA с id: {}", id);
        return mpaStorage.findById(id);
    }
}
