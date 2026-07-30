package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record SyntaxFailure<T>(List<OpenApiDiagnostic> diagnostics)
        implements SyntaxResult<T> {

    public SyntaxFailure {
        diagnostics = ResultDiagnostics.forFailure(diagnostics);
    }
}
