package ru.luttsev.studio.core.model.schema;

import java.util.Objects;

public record SchemaFormat(String value) {

    public SchemaFormat {
        Objects.requireNonNull(value, "value must not be null");
    }
}
