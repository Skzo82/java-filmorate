package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Создание пользователя с полной валидацией
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Создание пользователя: {}", user);
        return userService.createUser(user);
    }

    // Обновление пользователя: только ID обязателен, остальное — опционально
    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Обновление пользователя: {}", user);
        return userService.updateUser(user);
    }

    // Получение всех пользователей
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    // Получение пользователя по ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        log.info("Запрошен пользователь с ID: {}", id);
        return userService.findById(id);
    }
}
