package ru.luttsev.studio.application.workspace;

import java.util.Objects;
import java.util.UUID;
import ru.luttsev.studio.core.model.OpenApiDocument;

public record DocumentSession(
        UUID id,
        long revision,
        OpenApiDocument document) {

    public DocumentSession {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(document, "document must not be null");
        if (revision < 0) {
            throw new IllegalArgumentException("revision must not be negative");
        }
    }
}
