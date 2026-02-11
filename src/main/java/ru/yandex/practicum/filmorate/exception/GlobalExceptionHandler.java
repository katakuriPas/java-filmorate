package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ValidationException.class, DuplicatedDataException.class, ConditionsNotMetException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleBadRequestException(Exception e) {
        String prefix = switch (e) {
            case ValidationException ve -> "Ошибка валидации:";
            case DuplicatedDataException de -> "Ошибка данных:";
            case ConditionsNotMetException cne -> "Условия не выполнены:";
            default -> "Ошибка:";
        };
        return new ErrorResponse(prefix + " " + e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        return new ErrorResponse("Объект не найден: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ErrorResponse handleGenericException(Exception e) {
        return new ErrorResponse("Внутренняя ошибка сервера: " + e.getMessage());
    }
}
