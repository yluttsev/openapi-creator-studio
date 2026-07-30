package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record ExportFailure(List<OpenApiDiagnostic> diagnostics)
        implements ExportResult {

    public ExportFailure {
        diagnostics = ResultDiagnostics.forFailure(diagnostics);
    }
}
