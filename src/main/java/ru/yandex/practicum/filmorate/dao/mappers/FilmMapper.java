package ru.yandex.practicum.filmorate.dao.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
@Component
public class FilmMapper implements RowMapper<Film> {

    private final MpaMapper mpaMapper;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getLong("mpa_id"), null))
                .build();
    }
}