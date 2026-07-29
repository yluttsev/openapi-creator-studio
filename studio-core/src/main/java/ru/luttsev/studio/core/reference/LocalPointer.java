package ru.luttsev.studio.core.reference;

import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;

record LocalPointer(DocumentPath path) implements ParsedReference {

    LocalPointer {
        Objects.requireNonNull(path, "path must not be null");
    }
}
