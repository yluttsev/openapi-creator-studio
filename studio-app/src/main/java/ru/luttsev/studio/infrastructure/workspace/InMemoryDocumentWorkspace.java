package ru.luttsev.studio.infrastructure.workspace;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.application.workspace.DocumentWorkspace;
import ru.luttsev.studio.application.workspace.WorkspaceCommandExecution;
import ru.luttsev.studio.core.command.CommandExecutor;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.model.OpenApiDocument;

@Component
public final class InMemoryDocumentWorkspace implements DocumentWorkspace {

    private final ConcurrentMap<UUID, StoredDocument> documents =
            new ConcurrentHashMap<>();
    private final CommandExecutor commandExecutor = new CommandExecutor();

    @Override
    public DocumentSession create(OpenApiDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        while (true) {
            UUID id = UUID.randomUUID();
            StoredDocument storedDocument = new StoredDocument(id, document);
            if (documents.putIfAbsent(id, storedDocument) == null) {
                return storedDocument.snapshot();
            }
        }
    }

    @Override
    public Optional<DocumentSession> find(UUID documentId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        StoredDocument storedDocument = documents.get(documentId);
        return storedDocument == null
                ? Optional.empty()
                : Optional.of(storedDocument.snapshot());
    }

    @Override
    public Optional<WorkspaceCommandExecution> execute(
            UUID documentId,
            long expectedRevision,
            DocumentCommand command) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(command, "command must not be null");
        StoredDocument storedDocument = documents.get(documentId);
        return storedDocument == null
                ? Optional.empty()
                : Optional.of(storedDocument.execute(
                        expectedRevision,
                        command,
                        commandExecutor));
    }

    @Override
    public boolean delete(UUID documentId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        return documents.remove(documentId) != null;
    }
}
