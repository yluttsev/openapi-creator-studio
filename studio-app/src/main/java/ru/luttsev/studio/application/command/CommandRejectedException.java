package ru.luttsev.studio.application.command;

import java.util.List;
import lombok.Getter;
import ru.luttsev.studio.core.command.result.CommandIssue;

@Getter
public final class CommandRejectedException extends RuntimeException {

    private final List<CommandIssue> issues;

    public CommandRejectedException(List<CommandIssue> issues) {
        super("Document command was rejected");
        this.issues = List.copyOf(issues);
    }
}
