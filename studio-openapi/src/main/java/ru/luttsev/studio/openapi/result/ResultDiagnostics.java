package ru.luttsev.studio.openapi.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

final class ResultDiagnostics {

    private ResultDiagnostics() {
    }

    static List<OpenApiDiagnostic> forSuccess(
            List<OpenApiDiagnostic> diagnostics) {
        List<OpenApiDiagnostic> copy = copyOf(diagnostics);
        if (containsError(copy)) {
            throw new IllegalArgumentException(
                    "successful result must not contain error diagnostics");
        }
        return copy;
    }

    static List<OpenApiDiagnostic> forFailure(
            List<OpenApiDiagnostic> diagnostics) {
        List<OpenApiDiagnostic> copy = copyOf(diagnostics);
        if (!containsError(copy)) {
            throw new IllegalArgumentException(
                    "failed result must contain at least one error diagnostic");
        }
        return copy;
    }

    private static List<OpenApiDiagnostic> copyOf(
            List<OpenApiDiagnostic> diagnostics) {
        Objects.requireNonNull(diagnostics, "diagnostics must not be null");
        return List.copyOf(diagnostics);
    }

    private static boolean containsError(
            List<OpenApiDiagnostic> diagnostics) {
        return diagnostics.stream()
                .anyMatch(diagnostic ->
                        diagnostic.severity() == DiagnosticSeverity.ERROR);
    }
}
