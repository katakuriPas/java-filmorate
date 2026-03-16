package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Превращает исключение в HTTP 500
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}