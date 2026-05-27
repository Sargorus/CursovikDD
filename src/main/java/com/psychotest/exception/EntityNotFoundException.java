package main.java.com.psychotest.exception;

/**
 * Бросается когда запрошенная сущность отсутствует в базе данных.
 *
 * <p>Примеры: тест с указанным ID не найден, пользователь не существует,
 * сессия была удалена до завершения расчёта результатов.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityType, int id) {
        super(entityType + " с ID " + id + " не найден(а) в базе данных");
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
