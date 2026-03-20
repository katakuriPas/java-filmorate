package ru.yandex.practicum.filmorate.dao;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GlobalFeedTrigger implements Trigger {
    private String tableName;

    @Override
    public void init(Connection conn, String schemaName, String triggerName,
                     String tableName, boolean before, int type) {
        this.tableName = tableName;

    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        System.out.println("TRIGGER");
        Long userId = 0L;
        String eventType = "";
        String operation = "";
        Long entityId = 0L;

        Object[] row;

        if (oldRow == null) {
            operation = "ADD";
            row = newRow;
        } else if (newRow == null) {
            operation = "REMOVE";
            row = oldRow;
        } else {
            operation = "UPDATE";
            row = newRow;
        }

        switch (this.tableName.toUpperCase()) {
            case "FILM_LIKES" -> {
                userId = (Long) row[0];
                eventType = "LIKE";
                entityId = (Long) row[1];
            }
            case "FRIENDS" -> {
                userId = (Long) row[0];
                eventType = "FRIEND";
                entityId = (Long) row[1];
            }
            case "REVIEWS" -> {
                userId = (Long) row[4];
                eventType = "REVIEW";
                entityId = (Long) row[5];
            }
        }

        String sql = "INSERT INTO feed (user_id, event_type, entity_id, operation) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, eventType);
            ps.setLong(3, entityId);
            ps.setString(4, operation);
            ps.executeUpdate();
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void remove() {
    }
}


//        "userId": 123,
//        "eventType": "LIKE", // одно из значениий LIKE, REVIEW или FRIEND
//        "operation": "REMOVE", // одно из значениий REMOVE, ADD, UPDATE
//        "entityId": 1234   // идентификатор сущности, с которой произошло событие