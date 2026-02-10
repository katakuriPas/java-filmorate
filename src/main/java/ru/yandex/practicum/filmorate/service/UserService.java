package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserStorage userStorage;

    public Collection<User> findAllUser() {
        return userStorage.findAllUser();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User newUser) {
        return userStorage.updateUser(newUser);
    }

    public void addFriend(Long id, Long newFriendId) {
        User user = userStorage.getUserById(id);
        User newFriend = userStorage.getUserById(newFriendId);

        user.getFriends().add(newFriendId);
        newFriend.getFriends().add(id);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);

        if (user.getFriends() != null) {
            user.getFriends().remove(friendId);
        }
        if (friend.getFriends() != null) {
            friend.getFriends().remove(id);
        }
    }

    public Collection<User> commonIdFriends(Long id, Long otherId) {
        User userOne = userStorage.getUserById(id);
        User userTwo = userStorage.getUserById(otherId);

        if (userOne.getFriends() == null || userTwo.getFriends() == null) {
            return Collections.emptyList();
        }

        Set<Long> commonIdFriends = new HashSet<>(userOne.getFriends());
        commonIdFriends.retainAll(userTwo.getFriends());
        return commonIdFriends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> listFriends (Long id) {
        User user = userStorage.getUserById(id);
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}
