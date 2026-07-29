package ru.luttsev.studio.core.validation;

import java.util.Objects;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record ValidationIssue(
        ValidationCode code,
        ValidationSeverity severity,
        String message,
        DocumentPath path) {

    public ValidationIssue {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(path, "path must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
