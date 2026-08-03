package ru.luttsev.studio.application.workspace;

import java.util.Objects;
import ru.luttsev.studio.core.command.result.CommandResult;

public record WorkspaceCommandExecution(
        DocumentSession session,
        CommandResult result) {

    public WorkspaceCommandExecution {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(result, "result must not be null");
    }
}
