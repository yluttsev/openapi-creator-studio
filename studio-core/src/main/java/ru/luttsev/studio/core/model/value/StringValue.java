package ru.luttsev.studio.core.model.value;

import java.util.Objects;

public record StringValue(String value) implements DocumentValue {

    public StringValue {
        Objects.requireNonNull(value, "value must not be null");
    }
}
