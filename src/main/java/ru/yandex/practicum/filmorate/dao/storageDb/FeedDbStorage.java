package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.List;

@Component
@Profile("database")
@Slf4j
public class FeedDbStorage implements FeedStorage {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final RowMapper<Feed> mapper;

    public FeedDbStorage(NamedParameterJdbcTemplate namedJdbc, RowMapper<Feed> mapper) {
        this.namedJdbc = namedJdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Feed> findFeed(Long id) {
        String sql = "SELECT * FROM feed WHERE user_id IN (:friendId)";

        SqlParameterSource parameters = new MapSqlParameterSource("friendId", id);

        return namedJdbc.query(sql, parameters, mapper);
    }
}
