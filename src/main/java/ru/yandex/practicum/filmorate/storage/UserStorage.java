package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAllUser();

    User createUser(User user);

    User updateUser(User newUser);

    Optional<User> getUserById(Long id);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByLoginExcludeId(String login, Long id);
}
