package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = ru.yandex.practicum.filmorate.model.Feed.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .eventType(rs.getString("event_type"))
                .entityId(rs.getLong("entity_id"))
                .operation(rs.getString("operation"))
                .timestamp(rs.getTimestamp("time_and_data").getTime())
                .build();

        return feed;
    }
}