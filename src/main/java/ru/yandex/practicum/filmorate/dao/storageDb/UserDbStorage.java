package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Component
@Profile("database")
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String INSERT = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    // SQL для проверки существования
    private static final String EXISTS_BY_LOGIN = "SELECT COUNT(*) FROM users WHERE login = ?";
    private static final String EXISTS_BY_EMAIL = "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String EXISTS_BY_LOGIN_EXCLUDE_ID = "SELECT COUNT(*) FROM users WHERE login = ? AND id != ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User createUser(User user) {
        long id = insert(INSERT, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(UPDATE, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Collection<User> findAllUser() {
        return findMany(FIND_ALL);
    }

    @Override
    public boolean existsByLogin(String login) {
        Integer count = jdbc.queryForObject(EXISTS_BY_LOGIN, Integer.class, login);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(EXISTS_BY_EMAIL, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByLoginExcludeId(String login, Long excludeId) {
        Integer count = jdbc.queryForObject(EXISTS_BY_LOGIN_EXCLUDE_ID, Integer.class, login, excludeId);
        return count != null && count > 0;
    }
}