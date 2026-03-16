package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.Collection;

@Component
@Profile("database")
@RequiredArgsConstructor
@Slf4j
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    private static final String ADD_FRIEND =
            "INSERT INTO friends (user_id, friend_id, status_id) " +
                    "VALUES (?, ?, (SELECT id FROM friendship_status WHERE friendshipStatus = 'PENDING'))";

    private static final String DELETE_FRIEND =
            "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

    private static final String LIST_FRIENDS =
            "SELECT u.* FROM users u " +
                    "JOIN friends f ON u.id = f.friend_id " +
                    "WHERE f.user_id = ? " +
                    "ORDER BY u.name";

    private static final String COMMON_FRIENDS =
            "SELECT u.* FROM users u " +
                    "JOIN friends f1 ON u.id = f1.friend_id " +
                    "JOIN friends f2 ON u.id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ? " +
                    "ORDER BY u.name";

    private static final String CHECK_USER_EXISTS = "SELECT COUNT(*) FROM users WHERE id = ?";

    private static final String CHECK_FRIENDSHIP_EXISTS =
            "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";

    @Override
    public void addFriend(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        // Проверяем, нет ли уже такой связи
        Integer count = jdbc.queryForObject(CHECK_FRIENDSHIP_EXISTS, Integer.class, userId, friendId);
        if (count != null && count > 0) {
            throw new ValidationException("Пользователь уже есть в списке друзей");
        }

        jdbc.update(ADD_FRIEND, userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);

        int rowsDeleted = jdbc.update(DELETE_FRIEND, userId, friendId);
        if (rowsDeleted > 0) {
            log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        } else {
            log.warn("Попытка удалить несуществующую дружбу: {} → {}", userId, friendId);
        }
    }

    @Override
    public Collection<User> listFriends(Long userId) {
        validateUserExists(userId);
        return jdbc.query(LIST_FRIENDS, mapper, userId);
    }

    @Override
    public Collection<User> commonFriends(Long userId, Long otherId) {
        validateUserExists(userId);
        validateUserExists(otherId);
        return jdbc.query(COMMON_FRIENDS, mapper, userId, otherId);
    }

    private void validateUserExists(Long userId) {
        Integer count = jdbc.queryForObject(CHECK_USER_EXISTS, Integer.class, userId);
        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void validateUsersExist(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);
    }
}
