package ru.luttsev.studio.core.model.response;

import java.util.Objects;

public record ResponseKey(String value) {

    public static final ResponseKey DEFAULT = new ResponseKey("default");

    public ResponseKey {
        Objects.requireNonNull(value, "value must not be null");
    }
}
