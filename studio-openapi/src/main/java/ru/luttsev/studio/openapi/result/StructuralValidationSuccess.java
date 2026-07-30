package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record StructuralValidationSuccess(
        List<OpenApiDiagnostic> diagnostics)
        implements StructuralValidationResult {

    public StructuralValidationSuccess {
        diagnostics = ResultDiagnostics.forSuccess(diagnostics);
    }
}
