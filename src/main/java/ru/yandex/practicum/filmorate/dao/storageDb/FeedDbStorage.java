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
    private static final String CHECK_USER = "SELECT COUNT(*) FROM users WHERE id = :userId";
    private static final String FIND_FEED = "SELECT * FROM feed WHERE user_id IN (:friendId)";
    private static final String SAVE_FEED = "INSERT INTO feed (user_id, event_type, entity_id, operation, event_timestamp) " +
            "VALUES (:userId, :eventType, :entityId, :operation, :timestamp)";

    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Feed> mapper;

    public FeedDbStorage(NamedParameterJdbcTemplate namedJdbc, RowMapper<Feed> mapper) {
        this.namedJdbc = namedJdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Feed> findFeed(Long id) {

        Integer count = namedJdbc.queryForObject(CHECK_USER, new MapSqlParameterSource("userId", id), Integer.class);

        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        SqlParameterSource parameters = new MapSqlParameterSource("friendId", id);

        return namedJdbc.query(FIND_FEED, parameters, mapper);
    }

    @Override
    public void saveFeed(Long id, String eventType, Long entityId, String operation) {

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", id)
                .addValue("eventType", eventType)
                .addValue("entityId", entityId)
                .addValue("operation", operation)
                .addValue("timestamp", System.currentTimeMillis()); // Критично для тестов Postman!

        namedJdbc.update(SAVE_FEED, params);
    }
}
