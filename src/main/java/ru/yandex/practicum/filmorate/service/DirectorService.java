package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storageDb.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {

    private final DirectorDbStorage directorDbStorage;

    public List<Director> findAllDirectors() {
        log.info("Запрос на получение всех режиссеров");
        return directorDbStorage.findAll();
    }

    public Director getDirectorById(Long id) {
        log.info("Запрос режиссёра с id={}", id);
        return directorDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }


    public Director createDirector(Director director) {
        log.info("Создание режиссера: {}", director.getName());
        validateDirector(director);
        return directorDbStorage.create(director);
    }

    public Director updateDirector(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("ID режиссёра должен быть указан");
        }
        getDirectorById(director.getId());
        log.info("Обновление режиссёра с id={}", director.getId());
        validateDirector(director);
        return directorDbStorage.update(director);
    }

    public void deleteDirector(Long id) {
        log.info("Удаление режиссёра с id={}", id);
        getDirectorById(id);
        directorDbStorage.delete(id);
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

    }
}