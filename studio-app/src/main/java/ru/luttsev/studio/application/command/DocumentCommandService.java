package ru.luttsev.studio.application.command;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.workspace.DocumentWorkspace;
import ru.luttsev.studio.application.workspace.WorkspaceCommandExecution;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandRejected;
import ru.luttsev.studio.core.command.result.CommandSucceeded;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DocumentCommandService {

    private final DocumentWorkspace workspace;

    public ExecutedDocumentCommand execute(
            UUID documentId,
            long expectedRevision,
            DocumentCommand command) {
        String commandType = command.getClass().getSimpleName();
        log.debug(
                "Executing {} on document {} (expected revision {})",
                commandType,
                documentId,
                expectedRevision);
        WorkspaceCommandExecution execution = workspace.execute(
                        documentId,
                        expectedRevision,
                        command)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        if (execution.result() instanceof CommandRejected rejected) {
            throw new CommandRejectedException(rejected.issues());
        }
        CommandSucceeded succeeded = (CommandSucceeded) execution.result();
        log.info(
                "Executed {} on document {}, new revision {}, {} path(s) changed",
                commandType,
                documentId,
                execution.session().revision(),
                succeeded.changedPaths().size());
        return new ExecutedDocumentCommand(
                execution.session().revision(),
                succeeded.changedPaths());
    }
}
