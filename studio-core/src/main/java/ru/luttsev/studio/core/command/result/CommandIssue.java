package ru.luttsev.studio.core.command.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record CommandIssue(
        CommandCode code,
        String message,
        DocumentPath path,
        List<DocumentPath> relatedPaths) {

    public CommandIssue(
            CommandCode code,
            String message,
            DocumentPath path) {
        this(code, message, path, List.of());
    }

    public CommandIssue {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(path, "path must not be null");
        relatedPaths = List.copyOf(relatedPaths);
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
