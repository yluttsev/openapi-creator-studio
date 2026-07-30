package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record AdapterFailure<T>(List<OpenApiDiagnostic> diagnostics)
        implements AdapterResult<T> {

    public AdapterFailure {
        diagnostics = ResultDiagnostics.forFailure(diagnostics);
    }
}
