package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public sealed interface ExportResult
        permits ExportSuccess, ExportFailure {

    List<OpenApiDiagnostic> diagnostics();
}
