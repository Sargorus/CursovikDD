package main.java.com.psychotest.exception;

/**
 * Бросается при ошибках, связанных с сессией тестирования:
 * невозможность создать сессию, сессия в неверном статусе и т.п.
 */
public class SessionException extends BusinessException {

    public SessionException(String message) {
        super(message);
    }

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
