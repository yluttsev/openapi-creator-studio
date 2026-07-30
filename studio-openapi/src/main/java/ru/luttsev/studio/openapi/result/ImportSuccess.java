package ru.luttsev.studio.openapi.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record ImportSuccess(
        OpenApiDocument document,
        List<OpenApiDiagnostic> diagnostics)
        implements ImportResult {

    public ImportSuccess {
        Objects.requireNonNull(document, "document must not be null");
        diagnostics = ResultDiagnostics.forSuccess(diagnostics);
    }
}
