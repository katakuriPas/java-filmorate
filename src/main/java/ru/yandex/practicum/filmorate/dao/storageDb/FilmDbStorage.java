package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Component
@Profile("database")
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final int LIMIT_FILMS = 10;

    private static final String FIND_BY_ID =
            "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE f.id = ?";

    private static final String FIND_ALL =
            "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id";

    private static final String INSERT = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String LIKE_FILM = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE user_id = ? AND film_id = ?";

    private static final String MOST_POPULAR_FILMS =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC " +
                    "FETCH FIRST ? ROWS ONLY";

    private static final String MOST_POPULAR_FILMS_WITH_FILTER =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE (? IS NULL OR YEAR(f.release_date) = ?) " +
                    "  AND (? IS NULL OR EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.id AND fg.genre_id = ?)) " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC " +
                    "FETCH FIRST ? ROWS ONLY";

    private static final String FIND_GENRES_BY_FILM_ID =
            "SELECT g.* FROM genre g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ?";

    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String FIND_DIRECTORS_BY_FILM_ID =
            "SELECT d.* FROM directors d " +
                    "JOIN film_directors fd ON d.id = fd.director_id " +
                    "WHERE fd.film_id = ? ORDER BY d.id";

    private static final String INSERT_FILM_DIRECTOR = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS = "DELETE FROM film_directors WHERE film_id = ?";

    // Новый запрос для функции поиска
    private static final String SEARCH_FILMS =
            "SELECT DISTINCT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.id " +
                    "WHERE 1=1 ";

    private static final String FIND_BY_DIRECTOR_SORTED_YEAR =
            "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date ASC";

    private static final String FIND_BY_DIRECTOR_SORTED_LIKES =
            "SELECT f.*, m.name as mpa_name, COUNT(fl.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY likes_count DESC";

    private final JdbcTemplate jdbc;
    private final GenreMapper genreMapper;
    private final FilmMapper filmMapper;
    private final DirectorMapper directorMapper;

    public FilmDbStorage(
            JdbcTemplate jdbc,
            RowMapper<Film> mapper,
            JdbcTemplate jdbc1,
            GenreMapper genreMapper,
            FilmMapper filmMapper,
            DirectorMapper directorMapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc1;
        this.genreMapper = genreMapper;
        this.filmMapper = filmMapper;
        this.directorMapper = directorMapper;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> filmOptional = findOne(FIND_BY_ID, id);
        filmOptional.ifPresent(this::loadGenresAndDirectors);
        return filmOptional;
    }

    @Override
    public Collection<Film> findAllFilm() {
        List<Film> films = findMany(FIND_ALL);
        films.forEach(this::loadGenresAndDirectors);
        return films;
    }

    private void loadGenresAndDirectors(Film film) {
        List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, genreMapper, film.getId());
        film.setGenres(new HashSet<>(genres));

        List<Director> directors = jdbc.query(FIND_DIRECTORS_BY_FILM_ID, directorMapper, film.getId());
        film.setDirectors(new HashSet<>(directors));
    }

    @Override
    public void likeFilm(Long filmId, Long userId) {
        jdbc.update(LIKE_FILM, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE, userId, filmId);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        log.info("Поиск фильмов: query='{}', by='{}'", query, by);

        StringBuilder sql = new StringBuilder(SEARCH_FILMS);
        List<Object> params = new ArrayList<>();

        String[] searchBy = by.toLowerCase().split(",");
        boolean searchByTitle = false;
        boolean searchByDirector = false;

        for (String s : searchBy) {
            s = s.trim();
            if (s.equals("title")) {
                searchByTitle = true;
            } else if (s.equals("director")) {
                searchByDirector = true;
            }
        }

        if (searchByTitle && searchByDirector) {
            sql.append("AND (LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)) ");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        } else if (searchByTitle) {
            sql.append("AND LOWER(f.name) LIKE LOWER(?) ");
            params.add("%" + query + "%");
        } else if (searchByDirector) {
            sql.append("AND LOWER(d.name) LIKE LOWER(?) ");
            params.add("%" + query + "%");
        } else {
            throw new IllegalArgumentException("Параметр 'by' должен содержать 'title' и/или 'director'");
        }

        sql.append("ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC");

        log.debug("SQL запрос: {}", sql);

        List<Film> films = jdbc.query(sql.toString(), filmMapper, params.toArray());

        for (Film film : films) {
            loadFilmDetails(film);
        }

        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    @Override
    public List<Film> mostPopularFilms(Integer count) {
        return jdbc.query(MOST_POPULAR_FILMS, filmMapper, Objects.requireNonNullElse(count, LIMIT_FILMS));
    }

    @Override
    public List<Film> mostPopularFilms(Integer count, Long genreId, Integer year) {
        List<Film> films = jdbc.query(MOST_POPULAR_FILMS_WITH_FILTER, filmMapper, year, year, genreId, genreId, count);
        films.forEach(this::loadGenresAndDirectors);
        return films;
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        saveFilmGenres(id, film.getGenres());

        saveFilmDirectors(id, film.getDirectors());

        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        update(UPDATE,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );

        saveFilmGenres(newFilm.getId(), newFilm.getGenres());

        saveFilmDirectors(newFilm.getId(), newFilm.getDirectors());

        return getFilmById(newFilm.getId())
                .orElseThrow(() -> new InternalServerException("Не удалось получить обновлённый фильм"));
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        jdbc.update(DELETE_FILM_GENRES, filmId);

        // Добавляем новые
        if (genres != null && !genres.isEmpty()) {
            for (Genre genre : genres) {
                if (genre.getId() != null) {
                    jdbc.update(INSERT_FILM_GENRE, filmId, genre.getId());
                }
            }
        }
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYear(Long directorId) {
        List<Film> films = jdbc.query(FIND_BY_DIRECTOR_SORTED_YEAR, filmMapper, directorId);
        films.forEach(this::loadGenresAndDirectors);
        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(Long directorId) {
        List<Film> films = jdbc.query(FIND_BY_DIRECTOR_SORTED_LIKES, filmMapper, directorId);
        films.forEach(this::loadGenresAndDirectors);
        return films;
    }

    @Override
    public void deleteFilm(Long id) {

        String sql = "DELETE FROM films WHERE id = ?";

        try {
            int rowsAffected = jdbc.update(sql, id);

            if (rowsAffected == 0) {
                throw new NotFoundException("Фильма с id " + id + " нет в базе");
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Ошибка при работе с БД: " + e.getMessage(), e);
        }
    }

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        jdbc.update(DELETE_FILM_DIRECTORS, filmId);

        if (directors != null && !directors.isEmpty()) {
            for (Director director : directors) {
                if (director.getId() != null) {
                    jdbc.update(INSERT_FILM_DIRECTOR, filmId, director.getId());
                }
            }
        }
    }

    private void loadFilmDetails(Film film) {
        List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, genreMapper, film.getId());
        film.setGenres(new HashSet<>(genres));

        List<Director> directors = jdbc.query(FIND_DIRECTORS_BY_FILM_ID, directorMapper, film.getId());
        film.setDirectors(new HashSet<>(directors));
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        log.info("Получение фильмов режиссёра {} с сортировкой по {}", directorId, sortBy);

        String sql;
        if ("year".equals(sortBy)) {
            sql = "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
        } else { // likes
            sql = "SELECT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC";
        }

        List<Film> films = jdbc.query(sql, filmMapper, directorId);

        for (Film film : films) {
            List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, genreMapper, film.getId());
            film.setGenres(new HashSet<>(genres));

            List<Director> directors = jdbc.query(FIND_DIRECTORS_BY_FILM_ID, directorMapper, film.getId());
            film.setDirectors(new HashSet<>(directors));
        }

        log.info("Найдено {} фильмов для режиссёра {}", films.size(), directorId);
        return films;
    }
}
