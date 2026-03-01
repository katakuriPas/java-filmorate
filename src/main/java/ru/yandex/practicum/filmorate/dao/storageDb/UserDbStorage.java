package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Profile("database")
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String INSERT = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    // SQL для проверки логина
    private static final String EXISTS_BY_LOGIN = "SELECT COUNT(*) FROM users WHERE login = ?";
    private static final String EXISTS_BY_EMAIL = "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String EXISTS_BY_LOGIN_EXCLUDE_ID = "SELECT COUNT(*) FROM users WHERE login = ? AND id != ?";

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

    //Общие методы для User
    private Optional<User> findOne(String query, Object... params) {
        try {
            User result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private List<User> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    private void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }
}