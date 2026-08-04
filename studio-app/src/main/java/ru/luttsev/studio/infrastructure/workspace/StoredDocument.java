package ru.luttsev.studio.infrastructure.workspace;

import java.util.Objects;
import java.util.UUID;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.application.workspace.RevisionConflictException;
import ru.luttsev.studio.application.workspace.WorkspaceCommandExecution;
import ru.luttsev.studio.core.command.CommandExecutor;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandResult;
import ru.luttsev.studio.core.command.result.CommandSucceeded;
import ru.luttsev.studio.core.model.OpenApiDocument;

final class StoredDocument {

    private final UUID id;
    private final OpenApiDocument document;
    private long revision;

    StoredDocument(UUID id, OpenApiDocument document) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.document = Objects.requireNonNull(
                document,
                "document must not be null");
    }

    synchronized DocumentSession snapshot() {
        return new DocumentSession(id, revision, document);
    }

    synchronized WorkspaceCommandExecution execute(
            long expectedRevision,
            DocumentCommand command,
            CommandExecutor commandExecutor) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(
                commandExecutor,
                "commandExecutor must not be null");
        if (expectedRevision != revision) {
            throw new RevisionConflictException(
                    id,
                    expectedRevision,
                    revision);
        }

        long nextRevision = Math.incrementExact(revision);
        CommandResult result = commandExecutor.execute(document, command);
        if (result instanceof CommandSucceeded) {
            revision = nextRevision;
        }
        return new WorkspaceCommandExecution(snapshot(), result);
    }

    synchronized void verifyRevision(long expectedRevision) {
        if (expectedRevision != revision) {
            throw new RevisionConflictException(
                    id,
                    expectedRevision,
                    revision);
        }
    }
}
