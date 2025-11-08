package io.github.jaehyeonhan.project.util;

public class ValidationUtils {

    public static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " should not be null");
        }
        return value;
    }
}
