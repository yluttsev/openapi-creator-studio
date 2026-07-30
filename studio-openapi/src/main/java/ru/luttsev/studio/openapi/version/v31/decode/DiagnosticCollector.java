package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.ArrayList;
import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

final class DiagnosticCollector {

    private final List<OpenApiDiagnostic> diagnostics = new ArrayList<>();

    void add(OpenApiDiagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(diagnostic ->
                        diagnostic.severity() == DiagnosticSeverity.ERROR);
    }

    List<OpenApiDiagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }
}
