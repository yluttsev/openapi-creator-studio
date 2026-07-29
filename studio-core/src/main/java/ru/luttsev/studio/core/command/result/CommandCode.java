package ru.luttsev.studio.core.command.result;

import java.util.Objects;

public record CommandCode(String value) {

    public CommandCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
