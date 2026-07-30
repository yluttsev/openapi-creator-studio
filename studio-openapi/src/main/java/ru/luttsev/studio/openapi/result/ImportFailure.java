package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record ImportFailure(List<OpenApiDiagnostic> diagnostics)
        implements ImportResult {

    public ImportFailure {
        diagnostics = ResultDiagnostics.forFailure(diagnostics);
    }
}
