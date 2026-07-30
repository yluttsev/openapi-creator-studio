package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record VersionDetectionFailure(
        List<OpenApiDiagnostic> diagnostics)
        implements VersionDetectionResult {

    public VersionDetectionFailure {
        diagnostics = ResultDiagnostics.forFailure(diagnostics);
    }
}
