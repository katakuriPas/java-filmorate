package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendshipStorage {

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> listFriends(Long userId);

    Collection<User> commonFriends(Long userId, Long otherId);
}
