package ru.luttsev.studio.application.document;

import java.util.UUID;
import lombok.Getter;

@Getter
public final class DocumentNotFoundException extends RuntimeException {

    private final UUID documentId;

    public DocumentNotFoundException(UUID documentId) {
        super("Document session not found: " + documentId);
        this.documentId = documentId;
    }
}
