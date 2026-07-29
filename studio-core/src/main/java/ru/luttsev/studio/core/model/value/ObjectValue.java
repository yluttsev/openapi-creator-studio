package ru.luttsev.studio.core.model.value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ObjectValue(Map<String, DocumentValue> values) implements DocumentValue {

    public ObjectValue {
        Objects.requireNonNull(values, "values must not be null");
        values.forEach((key, value) -> {
            Objects.requireNonNull(key, "object key must not be null");
            Objects.requireNonNull(value, "object value must not be null");
        });
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
