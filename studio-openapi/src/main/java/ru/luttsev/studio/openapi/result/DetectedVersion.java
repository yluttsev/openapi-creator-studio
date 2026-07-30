package ru.luttsev.studio.openapi.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record DetectedVersion(
        OpenApiVersion version,
        List<OpenApiDiagnostic> diagnostics)
        implements VersionDetectionResult {

    public DetectedVersion {
        Objects.requireNonNull(version, "version must not be null");
        diagnostics = ResultDiagnostics.forSuccess(diagnostics);
    }
}
