package ru.luttsev.studio.openapi.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record ExportSuccess(
        String content,
        List<OpenApiDiagnostic> diagnostics)
        implements ExportResult {

    public ExportSuccess {
        Objects.requireNonNull(content, "content must not be null");
        if (content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        diagnostics = ResultDiagnostics.forSuccess(diagnostics);
    }
}
