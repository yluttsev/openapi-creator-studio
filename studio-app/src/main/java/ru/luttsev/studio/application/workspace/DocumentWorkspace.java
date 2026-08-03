package ru.luttsev.studio.application.workspace;

import java.util.Optional;
import java.util.UUID;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.model.OpenApiDocument;

public interface DocumentWorkspace {

    DocumentSession create(OpenApiDocument document);

    Optional<DocumentSession> find(UUID documentId);

    Optional<WorkspaceCommandExecution> execute(
            UUID documentId,
            long expectedRevision,
            DocumentCommand command);

    boolean delete(UUID documentId);
}
