package ru.luttsev.studio.application.document;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record OpenedDocument(
        DocumentSession session,
        List<OpenApiDiagnostic> diagnostics) {

    public OpenedDocument {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(diagnostics, "diagnostics must not be null");
        diagnostics = List.copyOf(diagnostics);
    }
}
