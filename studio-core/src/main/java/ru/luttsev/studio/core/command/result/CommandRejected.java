package ru.luttsev.studio.core.command.result;

import java.util.List;
import java.util.Objects;

public record CommandRejected(List<CommandIssue> issues)
        implements CommandResult {

    public CommandRejected {
        Objects.requireNonNull(issues, "issues must not be null");
        issues = List.copyOf(issues);
        if (issues.isEmpty()) {
            throw new IllegalArgumentException("issues must not be empty");
        }
    }
}
