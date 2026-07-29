package ru.luttsev.studio.core.model.value;

import java.util.List;
import java.util.Objects;

public record ArrayValue(List<DocumentValue> values) implements DocumentValue {

    public ArrayValue {
        Objects.requireNonNull(values, "values must not be null");
        values = List.copyOf(values);
    }
}
