package ru.yandex.practicum.filmorate.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;         // HTTP-статус (например, 400, 404)
    private String message;     // Сообщение об ошибке
    private LocalDateTime timestamp;
}
