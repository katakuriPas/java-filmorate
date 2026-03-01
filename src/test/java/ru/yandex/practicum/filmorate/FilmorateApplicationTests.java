package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.storageDb.*;
import ru.yandex.practicum.filmorate.dao.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.dao.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FriendshipDbStorage.class,
        UserDbStorage.class,
        FilmDbStorage.class,
        GenreDbStorage.class,
        MpaDbStorage.class,
        UserMapper.class,
        FilmMapper.class,
        MpaMapper.class,
        GenreMapper.class
})
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final FriendshipDbStorage friendshipStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void cleanUp() {
        jdbc.update("DELETE FROM like_films");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM friends");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM friendship_status");
        jdbc.update("DELETE FROM genre");
        jdbc.update("DELETE FROM mpa");

        jdbc.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbc.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbc.update("ALTER TABLE genre ALTER COLUMN id RESTART WITH 1");
        jdbc.update("ALTER TABLE mpa ALTER COLUMN id RESTART WITH 1");

        jdbc.update("INSERT INTO users (email, login, name, birthday) VALUES ('a@b.com', 'user1', 'User1', '1990-01-01')");
        jdbc.update("INSERT INTO users (email, login, name, birthday) VALUES ('c@d.com', 'user2', 'User2', '1991-01-01')");
        jdbc.update("INSERT INTO users (email, login, name, birthday) VALUES ('e@f.com', 'user3', 'User3', '1992-01-01')");

        jdbc.update("INSERT INTO friendship_status (id, friendshipStatus) VALUES (1, 'PENDING'), (2, 'ACCEPTED')");

        jdbc.update("INSERT INTO genre (name) VALUES ('Комедия')");
        jdbc.update("INSERT INTO genre (name) VALUES ('Драма')");
        jdbc.update("INSERT INTO genre (name) VALUES ('Мультфильм')");
        jdbc.update("INSERT INTO genre (name) VALUES ('Триллер')");
        jdbc.update("INSERT INTO genre (name) VALUES ('Документальный')");
        jdbc.update("INSERT INTO genre (name) VALUES ('Боевик')");

        jdbc.update("INSERT INTO mpa (name) VALUES ('G')");
        jdbc.update("INSERT INTO mpa (name) VALUES ('PG')");
        jdbc.update("INSERT INTO mpa (name) VALUES ('PG-13')");
        jdbc.update("INSERT INTO mpa (name) VALUES ('R')");
        jdbc.update("INSERT INTO mpa (name) VALUES ('NC-17')");
    }

    @Test
    void genreStorage_findAll_shouldReturnAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        // Assert: проверяем, что жанры возвращаются
        assertThat(genres).isNotEmpty();
        assertThat(genres)
                .extracting("id")
                .doesNotContainNull();
    }

    @Test
    void mpaStorage_findById_shouldReturnMpa() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(1L);

        // Assert: проверяем, что рейтинг найден
        assertThat(mpaOptional).isPresent();
        assertThat(mpaOptional.get().getId()).isEqualTo(1L);
        assertThat(mpaOptional.get().getName()).isNotNull();
    }

    // === Тесты для Friendship ===

    @Test
    void addFriend_validUsers_shouldAddFriendship() {
        // Act & Assert
        friendshipStorage.addFriend(1L, 2L);

        // Проверяем, что запись появилась в БД
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friends WHERE user_id = 1 AND friend_id = 2",
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteFriend_existingFriendship_shouldRemoveFriendship() {
        // Arrange: создаём дружбу
        friendshipStorage.addFriend(1L, 2L);

        // Act: удаляем дружбу
        friendshipStorage.deleteFriend(1L, 2L);

        // Assert: проверяем, что запись удалена из БД
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friends WHERE user_id = 1 AND friend_id = 2",
                Integer.class
        );
        assertThat(count).isZero();
    }

    @Test
    void listFriends_userHasFriends_shouldReturnFriendsList() {
        // Arrange: создаём дружбу 1 -> 2 и 1 -> 3
        friendshipStorage.addFriend(1L, 2L);
        friendshipStorage.addFriend(1L, 3L);

        // Act
        Collection<User> friends = friendshipStorage.listFriends(1L);

        // Assert: проверяем количество и состав друзей
        assertThat(friends).hasSize(2);
        assertThat(friends)
                .extracting("id")
                .containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void commonFriends_bothUsersHaveCommonFriends_shouldReturnCommonFriends() {
        // Arrange: пользователь 3 является другом и для 1, и для 2
        friendshipStorage.addFriend(1L, 3L);
        friendshipStorage.addFriend(2L, 3L);

        // Act
        Collection<User> common = friendshipStorage.commonFriends(1L, 2L);

        // Assert: проверяем, что 3 — общий друг
        assertThat(common).hasSize(1);
        assertThat(common)
                .extracting("id")
                .containsExactly(3L);
    }

    // === Тесты для User ===

    @Test
    public void testFindUserById() {
        // Arrange: создаём тестового пользователя
        User testUser = User.builder()
                .email("test@example.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userStorage.createUser(testUser);

        // Act
        Optional<User> userOptional = userStorage.getUserById(1L);

        // Assert
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    void createUser() {
        User user = User.builder()
                .email("e@b.com")
                .login("login1")
                .name("Name1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userStorage.createUser(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("e@b.com");
    }

    @Test
    void updateUser() {
        User user = User.builder()
                .email("old@a.com")
                .login("oldlogin")
                .name("OldName")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User saved = userStorage.createUser(user);

        assertThat(saved.getId()).isNotNull();

        saved.setName("NewName");
        saved.setLogin("newlogin");

        User updated = userStorage.updateUser(saved);

        assertThat(updated.getName()).isEqualTo("NewName");
    }

    // === Тесты для Film ===

    @Test
    void createFilm() {
        Film film = Film.builder()
                .name("Film One")
                .description("Test description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120)
                .mpa(new Mpa(1L, null))
                .build();

        Film created = filmStorage.createFilm(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Film One");
        assertThat(created.getDuration()).isEqualTo(120);
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getId()).isEqualTo(1L);
    }

    @Test
    void getFilmById_found() {
        Film film = Film.builder()
                .name("Find Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2023, 5, 10))
                .duration(90)
                .mpa(new Mpa(2L, null))
                .build();
        Film saved = filmStorage.createFilm(film);

        var result = filmStorage.getFilmById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Find Film");
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getMpa().getId()).isEqualTo(2L);
    }

    @Test
    void getFilmById_notFound() {
        var result = filmStorage.getFilmById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void findAllFilm() {
        Film film1 = Film.builder()
                .name("Film A")
                .description("Desc A")
                .releaseDate(LocalDate.of(2024, 2, 1))
                .duration(100)
                .mpa(new Mpa(3L, "PG-13"))
                .build();

        Film film2 = Film.builder()
                .name("Film B")
                .description("Desc B")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .duration(110)
                .mpa(new Mpa(4L, "R"))
                .build();

        filmStorage.createFilm(film1);
        filmStorage.createFilm(film2);

        Collection<Film> films = filmStorage.findAllFilm();

        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting("name")
                .containsExactlyInAnyOrder("Film A", "Film B");
    }

    @Test
    void updateFilm() {
        Film film = Film.builder()
                .name("Old Name")
                .description("Old Desc")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(80)
                .mpa(new Mpa(1L, "G"))
                .build();
        Film saved = filmStorage.createFilm(film);

        saved.setName("New Name");
        saved.setDescription("New Desc");
        saved.setDuration(150);

        Film updated = filmStorage.updateFilm(saved);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        assertThat(updated.getDuration()).isEqualTo(150);
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }

    // === Тесты для LikeFilms ===
    @Test
    void likeFilm_validIds_shouldAddLike() {
        // Arrange: создаём фильм
        Film film = filmStorage.createFilm(Film.builder()
                .name("Film for Like")
                .description("Desc")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());

        // Act: ставим лайк от пользователя 1
        filmStorage.likeFilm(film.getId(), 1L);

        // Assert: проверяем, что запись появилась в таблице like_films
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM like_films WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(), 1L
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteLike_existingLike_shouldRemoveLike() {
        // Arrange: создаём фильм и ставим лайк
        Film film = filmStorage.createFilm(Film.builder()
                .name("Film for Unlike")
                .description("Desc")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());
        filmStorage.likeFilm(film.getId(), 1L);

        // Act: удаляем лайк
        filmStorage.deleteLike(film.getId(), 1L);

        // Assert: проверяем, что запись удалена из таблицы like_films
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM like_films WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(), 1L
        );
        assertThat(count).isZero();
    }

    @Test
    void mostPopularFilms_shouldReturnFilmsSortedByLikes() {
        // Arrange: создаём два фильма
        Film film1 = filmStorage.createFilm(Film.builder()
                .name("Popular Film")
                .description("Desc 1")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());

        Film film2 = filmStorage.createFilm(Film.builder()
                .name("Less Popular Film")
                .description("Desc 2")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());

        // Film1 получает 2 лайка, Film2 получает 0 лайков
        filmStorage.likeFilm(film1.getId(), 1L);
        filmStorage.likeFilm(film1.getId(), 2L);

        // Act: запрашиваем популярные фильмы
        List<Film> popular = filmStorage.mostPopularFilms(10);

        // Assert: проверяем порядок (сначала более популярный)
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film2.getId());
    }

    private Mpa createMpa(String name) {
        return switch (name) {
            case "G" -> new Mpa(1L, "G");
            case "PG" -> new Mpa(2L, "PG");
            case "PG-13" -> new Mpa(3L, "PG-13");
            case "R" -> new Mpa(4L, "R");
            case "NC-17" -> new Mpa(5L, "NC-17");
            default -> throw new IllegalArgumentException("Неизвестный рейтинг: " + name);
        };
    }
}