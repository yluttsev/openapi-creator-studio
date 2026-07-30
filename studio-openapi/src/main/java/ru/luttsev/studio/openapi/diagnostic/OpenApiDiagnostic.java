package ru.luttsev.studio.openapi.diagnostic;

import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record OpenApiDiagnostic(
        DiagnosticCode code,
        DiagnosticSeverity severity,
        DiagnosticPhase phase,
        String message,
        DocumentPath path,
        Optional<SourcePosition> sourcePosition) {

    public OpenApiDiagnostic(
            DiagnosticCode code,
            DiagnosticSeverity severity,
            DiagnosticPhase phase,
            String message,
            DocumentPath path) {
        this(
                code,
                severity,
                phase,
                message,
                path,
                Optional.empty());
    }

    public OpenApiDiagnostic {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(phase, "phase must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(sourcePosition, "sourcePosition must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
