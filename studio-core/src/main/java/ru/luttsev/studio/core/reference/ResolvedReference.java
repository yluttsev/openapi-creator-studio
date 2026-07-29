package ru.luttsev.studio.core.reference;

import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record ResolvedReference<T>(T value, DocumentPath targetPath)
        implements ReferenceResolution<T> {

    public ResolvedReference {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");
    }
}
