package ru.luttsev.studio.core.validation.support;

import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record ResolvedValue<T>(T value, DocumentPath path) {

    public ResolvedValue {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(path, "path must not be null");
    }
}
