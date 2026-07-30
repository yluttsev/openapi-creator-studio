package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record StructuralValidationFailure(
        List<OpenApiDiagnostic> diagnostics)
        implements StructuralValidationResult {

    public StructuralValidationFailure {
        diagnostics = ResultDiagnostics.forFailure(diagnostics);
    }
}
