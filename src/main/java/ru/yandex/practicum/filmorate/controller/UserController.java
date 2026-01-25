package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUser() {
        log.info("Запрос на получение всех пользователей. Количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Попытка создания фильма: {}", user.getName());

        validateUser(user);

        user.setId(getNextId());
        log.info("Пользователь успешно добавлен: {}", user.getName());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Редактирование пользователя: {}", newUser.getName());
        if (newUser.getId() == null) {
            log.warn("id не указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if(users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            boolean loginExists = users.values().stream()
                    .anyMatch(existingLogin -> existingLogin.getLogin().equals(newUser.getLogin()));
            if (loginExists) {
                log.warn("Ошибка валидации: Логин {} уже используется", newUser.getLogin());
                throw new DuplicatedDataException("Этот логин уже используется");
            }
            if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                log.warn("Ошибка валидации: email либо пустой, либо не содержит @");
                throw new ConditionsNotMetException("Email должен быть указан и содержать @");
            }
            if (newUser.getLogin().contains("Ошибка валидации: логин содержит пробелы")) {
                throw new ValidationException("Логин не должен содержать пробелы");
            }
            if (newUser.getName() == null || newUser.getName().isBlank()) {
                log.info("Пустое имя пользователя было заменено на логин {}", newUser.getLogin());
                newUser.setName(newUser.getLogin());
            }
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Ошибка валидации: день рождения будущего числа");
                throw new ValidationException("День рождения не может быть в будущем");
            }

            validateUser(newUser);

            User existingUser = users.get(newUser.getId());
            existingUser.setEmail(newUser.getEmail());
            existingUser.setLogin(newUser.getLogin());
            existingUser.setName(newUser.getName());
            existingUser.setBirthday(newUser.getBirthday());

            log.info("Пользователь с id = {} успешно обновлён", newUser.getId());
            return existingUser;
        }
        throw new NotFoundException("User с id = " + newUser.getId() + " не найден");
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: email не указан или не содержит @");
            throw new ValidationException("Email должен быть указан и содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: логин пустой или содержит пробелы");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы");
        }
        if(user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя было заменено на логин {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: некорректно введен день рождения");
            throw new ValidationException("День рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
