package ru.luttsev.studio.core.command;

import java.util.List;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandIssue;
import ru.luttsev.studio.core.command.result.CommandRejected;
import ru.luttsev.studio.core.command.result.CommandSucceeded;
import ru.luttsev.studio.core.navigation.DocumentPath;

public final class CommandResults {

    private CommandResults() {
    }

    public static CommandSucceeded succeeded(DocumentPath... changedPaths) {
        return new CommandSucceeded(List.of(changedPaths));
    }

    public static CommandRejected rejected(
            CommandCode code,
            String message,
            DocumentPath path) {
        return new CommandRejected(List.of(
                new CommandIssue(code, message, path)));
    }

    public static CommandRejected rejected(
            CommandCode code,
            String message,
            DocumentPath path,
            List<DocumentPath> relatedPaths) {
        return new CommandRejected(List.of(
                new CommandIssue(code, message, path, relatedPaths)));
    }
}
