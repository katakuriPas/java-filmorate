-- ============================================
-- 1. ОЧИСТКА: Удаляем таблицы в правильном порядке
-- ============================================
DROP TABLE IF EXISTS film_directors;
DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS reviews_likes;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS feed;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS friendship_status;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS mpa;

-- ============================================
-- 2. СОЗДАНИЕ ТАБЛИЦ
-- ============================================

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(40) NOT NULL,
    name VARCHAR(255),
    birthday DATE NOT NULL,
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_login_unique UNIQUE (login)
);

-- Справочник рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

-- Справочник жанров
CREATE TABLE IF NOT EXISTS genre (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Справочник статусов дружбы
CREATE TABLE IF NOT EXISTS friendship_status (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    friendshipStatus VARCHAR(20) NOT NULL UNIQUE
);

-- Таблица режиссёров
CREATE TABLE IF NOT EXISTS directors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT directors_name_unique UNIQUE (name)
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    release_date DATE NOT NULL,
    duration INTEGER CHECK (duration > 0),
    mpa_id INTEGER,
    CONSTRAINT fk_films_mpa FOREIGN KEY (mpa_id) REFERENCES mpa(id)
);

-- Связь фильмов и жанров (многие-ко-многим)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film_genres_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_film_genres_genre FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
);

-- Лайки фильмов (ИСПРАВЛЕНО: like_films -> film_likes)
CREATE TABLE IF NOT EXISTS film_likes (
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, film_id),
    CONSTRAINT fk_film_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_film_likes_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

-- Друзья пользователей
CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT friends_check CHECK (user_id <> friend_id),
    CONSTRAINT fk_friends_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friends_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friends_status FOREIGN KEY (status_id) REFERENCES friendship_status(id)
);

-- Связь фильмов и режиссёров (многие-ко-многим)
CREATE TABLE IF NOT EXISTS film_directors (
    film_id BIGINT NOT NULL,
    director_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, director_id),
    CONSTRAINT fk_film_directors_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_film_directors_director FOREIGN KEY (director_id) REFERENCES directors(id) ON DELETE CASCADE
);

-- Таблица отзывов
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    is_positive BOOLEAN DEFAULT TRUE,
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    useful_count INT DEFAULT 0,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews_likes (
    user_id BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    is_like BOOLEAN,

    PRIMARY KEY (user_id, review_id),

    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

--Лента событий
CREATE TABLE IF NOT EXISTS feed (
	event_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT NOT NULL,
	event_type VARCHAR(10) NOT NULL,
	entity_id BIGINT NOT NULL,
	operation VARCHAR(10) NOT NULL,
	event_timestamp BIGINT NOT NULL
);

-- ============================================
-- 3. ЗАПОЛНЕНИЕ СПРАВОЧНИКОВ
-- ============================================

-- MPA
INSERT INTO mpa (name) SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'G');
INSERT INTO mpa (name) SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'PG');
INSERT INTO mpa (name) SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'PG-13');
INSERT INTO mpa (name) SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'R');
INSERT INTO mpa (name) SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'NC-17');

-- Genre
INSERT INTO genre (name) SELECT 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Комедия');
INSERT INTO genre (name) SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Драма');
INSERT INTO genre (name) SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Мультфильм');
INSERT INTO genre (name) SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Триллер');
INSERT INTO genre (name) SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Документальный');
INSERT INTO genre (name) SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Боевик');

-- Friendship Status
INSERT INTO friendship_status (friendshipStatus) SELECT 'PENDING' WHERE NOT EXISTS (SELECT 1 FROM friendship_status WHERE friendshipStatus = 'PENDING');
INSERT INTO friendship_status (friendshipStatus) SELECT 'ACCEPTED' WHERE NOT EXISTS (SELECT 1 FROM friendship_status WHERE friendshipStatus = 'ACCEPTED');

-- ============================================
--Запус триггера
-- ============================================

----Лайки
--CREATE TRIGGER IF NOT EXISTS likes
--BEFORE INSERT, UPDATE, DELETE ON film_likes
--FOR EACH ROW
--CALL "ru.yandex.practicum.filmorate.dao.GlobalFeedTrigger";
--
----Друзья
--CREATE TRIGGER IF NOT EXISTS friend
--BEFORE INSERT, UPDATE, DELETE ON friends
--FOR EACH ROW
--CALL "ru.yandex.practicum.filmorate.dao.GlobalFeedTrigger";
--
----Отзывы
--CREATE TRIGGER IF NOT EXISTS review
--BEFORE INSERT, UPDATE, DELETE ON reviews
--FOR EACH ROW
--CALL "ru.yandex.practicum.filmorate.dao.GlobalFeedTrigger";