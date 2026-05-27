package main.java.com.psychotest.exception;

/**
 * Базовый класс для исключений бизнес-логики.
 *
 * <p>Бросается когда нарушено бизнес-правило, но причина не связана с БД:
 * объект не найден, некорректное состояние, невалидные входные данные.
 * Является непроверяемым (unchecked), чтобы не засорять сигнатуры сервисов
 * и контроллеров лишними {@code throws}-объявлениями.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
