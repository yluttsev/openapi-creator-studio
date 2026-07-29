package ru.luttsev.studio.core.navigation;

import java.util.Objects;

public record DocumentEntry(DocumentPath path, Object value) {

    public DocumentEntry {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }
}
