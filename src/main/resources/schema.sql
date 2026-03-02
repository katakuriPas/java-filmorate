-- ============================================
-- 1. ОЧИСТКА: Удаляем таблицы в правильном порядке
-- ============================================
DROP TABLE IF EXISTS like_films;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
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

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    release_date DATE NOT NULL,
    duration INTEGER CHECK (duration > 0),
    mpa_id INTEGER REFERENCES mpa(id)
);

-- Связь фильмов и жанров (многие-ко-многим)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genre(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

-- Лайки фильмов
CREATE TABLE IF NOT EXISTS like_films (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);

-- Друзья пользователей
CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status_id INTEGER NOT NULL REFERENCES friendship_status(id),
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT friends_check CHECK (user_id <> friend_id)
);

-- ============================================
-- 3. ЗАПОЛНЕНИЕ СПРАВОЧНИКОВ (Безопасно для H2)
-- ============================================

-- MPA
INSERT INTO mpa (name)
SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'G');
INSERT INTO mpa (name)
SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'PG');
INSERT INTO mpa (name)
SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'PG-13');
INSERT INTO mpa (name)
SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'R');
INSERT INTO mpa (name)
SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM mpa WHERE name = 'NC-17');

-- Genre
INSERT INTO genre (name)
SELECT 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Комедия');
INSERT INTO genre (name)
SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Драма');
INSERT INTO genre (name)
SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Мультфильм');
INSERT INTO genre (name)
SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Триллер');
INSERT INTO genre (name)
SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Документальный');
INSERT INTO genre (name)
SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genre WHERE name = 'Боевик');

-- Friendship Status
INSERT INTO friendship_status (friendshipStatus)
SELECT 'PENDING' WHERE NOT EXISTS (SELECT 1 FROM friendship_status WHERE friendshipStatus = 'PENDING');
INSERT INTO friendship_status (friendshipStatus)
SELECT 'ACCEPTED' WHERE NOT EXISTS (SELECT 1 FROM friendship_status WHERE friendshipStatus = 'ACCEPTED');