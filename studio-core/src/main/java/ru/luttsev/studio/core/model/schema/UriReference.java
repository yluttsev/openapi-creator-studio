package ru.luttsev.studio.core.model.schema;

import java.util.Objects;

public record UriReference(String value) {

    public UriReference {
        Objects.requireNonNull(value, "value must not be null");
    }
}
