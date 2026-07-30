package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public sealed interface VersionDetectionResult
        permits DetectedVersion, VersionDetectionFailure {

    List<OpenApiDiagnostic> diagnostics();
}
