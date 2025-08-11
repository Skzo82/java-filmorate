package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
// import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    // @Qualifier("mpaDbStorage")
    private final MpaService mpaService;

    // Получение списка всех рейтингов MPA
    @GetMapping
    public List<Mpa> getAll() {
        return mpaService.getAll();
    }

    // Получение рейтинга по id
    @GetMapping("/{id}")
    public Mpa getById(@PathVariable int id) {
        return mpaService.getById(id);
    }
}
