package ru.luttsev.studio.application.workspace;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class RevisionConflictException extends RuntimeException {

    private final UUID documentId;
    private final long expectedRevision;
    private final long actualRevision;

    public RevisionConflictException(
            UUID documentId,
            long expectedRevision,
            long actualRevision) {
        super("Expected revision " + expectedRevision
                + " for document " + documentId
                + ", but current revision is " + actualRevision);
        this.documentId = Objects.requireNonNull(
                documentId,
                "documentId must not be null");
        this.expectedRevision = expectedRevision;
        this.actualRevision = actualRevision;
    }
}
