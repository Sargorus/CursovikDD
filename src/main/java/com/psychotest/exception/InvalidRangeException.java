package main.java.com.psychotest.exception;

public class InvalidRangeException extends Exception {

    public InvalidRangeException(String message) {
        super(message);
    }

    // Для более детальной информации
    public InvalidRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    // Статические фабричные методы для разных ситуаций
    public static InvalidRangeException overlappingRange(String paramName, int newMin, int newMax,
                                                         int existingMin, int existingMax, String existingDesc) {
        return new InvalidRangeException(String.format(
                "Диапазон [%d-%d] для параметра '%s' пересекается с существующим диапазоном [%d-%d]: %s",
                newMin, newMax, paramName, existingMin, existingMax, existingDesc
        ));
    }

    public static InvalidRangeException invalidOrder(int min, int max) {
        return new InvalidRangeException(
                String.format("Минимальное значение (%d) не может быть больше максимального (%d)", min, max)
        );
    }

    public static InvalidRangeException outOfBounds(int value, int minBound, int maxBound) {
        return new InvalidRangeException(
                String.format("Значение %d выходит за допустимые пределы [%d-%d]", value, minBound, maxBound)
        );
    }
}
