package ru.yandex.practicum.filmorate.dao.storageDb;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;

@Component
@Profile("database")
public class FeedDbStorage implements FeedStorage {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Feed> mapper;

    public FeedDbStorage(NamedParameterJdbcTemplate namedJdbc, RowMapper<Feed> mapper) {
        this.namedJdbc = namedJdbc;
        this.mapper = mapper;
    }


    @Override
    public Collection<Feed> findFeed(Long id) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = :userId";
        Integer count = namedJdbc.queryForObject(checkUserSql, new MapSqlParameterSource("userId", id), Integer.class);

        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        String sql = "SELECT * FROM feed WHERE user_id IN (:friendId)";

        SqlParameterSource parameters = new MapSqlParameterSource("friendId", id);

        return namedJdbc.query(sql, parameters, mapper);
    }

    @Override
    public void saveFeed(Long id, String eventType, Long entityId, String operation) {
        String sql = "INSERT INTO feed (user_id, event_type, entity_id, operation, event_timestamp) " +
                "VALUES (:userId, :eventType, :entityId, :operation, :timestamp)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", id)
                .addValue("eventType", eventType)
                .addValue("entityId", entityId)
                .addValue("operation", operation)
                .addValue("timestamp", System.currentTimeMillis()); // Критично для тестов Postman!

        namedJdbc.update(sql, params);
    }
}
