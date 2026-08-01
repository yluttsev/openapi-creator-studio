package ru.luttsev.studio.openapi.internal.orchestration;

import java.util.List;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

public final class OrchestrationDiagnostics {

    private static final DocumentPath VERSION_PATH =
            DocumentPath.root().child("openapi");

    private OrchestrationDiagnostics() {
    }

    public static boolean hasErrors(
            List<OpenApiDiagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.severity() == DiagnosticSeverity.ERROR);
    }

    public static OpenApiDiagnostic unsupportedAdapter(
            OpenApiVersion version,
            DiagnosticPhase phase) {
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                DiagnosticSeverity.ERROR,
                phase,
                "No OpenAPI version adapter supports version "
                        + version.value(),
                VERSION_PATH);
    }
}
