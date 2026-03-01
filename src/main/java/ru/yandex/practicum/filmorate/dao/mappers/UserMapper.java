package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Scope("prototype")
public class UserMapper implements RowMapper<User>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
