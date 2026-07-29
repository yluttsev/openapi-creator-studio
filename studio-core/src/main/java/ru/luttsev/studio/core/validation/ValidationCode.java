package ru.luttsev.studio.core.validation;

import java.util.Objects;

public record ValidationCode(String value) {

    public ValidationCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
