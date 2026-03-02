package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public Collection<User> findAllUser() {
        log.info("Запрос на получение всех пользователей. Количество: {}", userStorage.findAllUser().size());
        return userStorage.findAllUser();
    }

    public User createUser(User user) {
        validateUser(user);

        if (userStorage.existsByLogin(user.getLogin())) {
            log.warn("Логин '{}' уже используется", user.getLogin());
            throw new DuplicatedDataException("Этот логин уже используется");
        }

        if (userStorage.existsByEmail(user.getEmail())) {
            log.warn("Email '{}' уже используется", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        return userStorage.createUser(user);
    }


    public User updateUser(User newUser) {
        log.info("Редактирование пользователя: {}", newUser.getName());

        if (newUser.getId() == null) {
            log.warn("id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        Optional<User> existing = userStorage.getUserById(newUser.getId());
        if (existing.isEmpty()) {
            throw new NotFoundException("User с id = " + newUser.getId() + " не найден");
        }

        validateUser(newUser);

        if (userStorage.existsByLoginExcludeId(newUser.getLogin(), newUser.getId())) {
            log.warn("Логин '{}' уже используется другим пользователем", newUser.getLogin());
            throw new DuplicatedDataException("Этот логин уже используется");
        }

        if (userStorage.existsByEmail(newUser.getEmail())) {
            log.warn("Email '{}' уже используется", newUser.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        userStorage.updateUser(newUser);
        log.info("Пользователь с id = {} успешно обновлён", newUser.getId());
        return newUser;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Запрос на добавление в друзья: {} → {}", userId, friendId);
        friendshipStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Запрос на удаление из друзей: {} → {}", userId, friendId);
        friendshipStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> listFriends(Long userId) {
        log.info("Запрос на получение списка друзей пользователя {}", userId);
        return friendshipStorage.listFriends(userId);
    }

    public Collection<User> commonFriends(Long userId, Long otherId) {
        log.info("Запрос на получение общих друзей: {} и {}", userId, otherId);
        return friendshipStorage.commonFriends(userId, otherId);
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
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя было заменено на логин {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: некорректно введен день рождения");
            throw new ValidationException("День рождения не может быть в будущем");
        }
    }
}
