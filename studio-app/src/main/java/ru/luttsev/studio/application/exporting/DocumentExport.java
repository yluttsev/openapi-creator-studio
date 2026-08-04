package ru.luttsev.studio.application.exporting;

import java.util.List;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

public record DocumentExport(
        long revision,
        OpenApiFormat format,
        OpenApiVersion targetVersion,
        String fileName,
        String content,
        List<OpenApiDiagnostic> diagnostics) {

    public DocumentExport {
        diagnostics = List.copyOf(diagnostics);
    }
}
