package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> findAllUser();
    User createUser(User user);
    User updateUser(User newUser);
    User getUserById(Long id);
}
