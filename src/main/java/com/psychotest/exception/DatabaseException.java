package main.java.com.psychotest.exception;

import java.sql.SQLException;

/**
 * Непроверяемая обёртка над {@link SQLException}.
 *
 * <p>Бросается из DAO- и сервис-слоя когда операция с базой данных
 * завершилась с ошибкой. Верхние слои (контроллеры, представления) могут
 * поймать её и показать пользователю понятное сообщение, не объявляя
 * {@code throws SQLException} по всей цепочке вызовов.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, SQLException cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }

    /** Возвращает исходный {@link SQLException}, если доступен. */
    public SQLException getSqlCause() {
        Throwable cause = getCause();
        return (cause instanceof SQLException) ? (SQLException) cause : null;
    }
}
