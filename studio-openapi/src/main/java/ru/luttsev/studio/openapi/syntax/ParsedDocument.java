package ru.luttsev.studio.openapi.syntax;

import java.util.Objects;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

public record ParsedDocument(
        ObjectValue root,
        OpenApiFormat format) {

    public ParsedDocument {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(format, "format must not be null");
    }
}
