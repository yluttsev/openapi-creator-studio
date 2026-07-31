package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

final class EncodeContext {

    private final OpenApiVersion targetVersion;
    private final DocumentPath path;
    private final List<OpenApiDiagnostic> diagnostics;

    private EncodeContext(
            OpenApiVersion targetVersion,
            DocumentPath path,
            List<OpenApiDiagnostic> diagnostics) {
        this.targetVersion = targetVersion;
        this.path = path;
        this.diagnostics = diagnostics;
    }

    static EncodeContext root(OpenApiVersion targetVersion) {
        Objects.requireNonNull(
                targetVersion,
                "targetVersion must not be null");
        return new EncodeContext(
                targetVersion,
                DocumentPath.root(),
                new ArrayList<>());
    }

    EncodeContext child(String segment) {
        Objects.requireNonNull(segment, "segment must not be null");
        return new EncodeContext(
                targetVersion,
                path.child(segment),
                diagnostics);
    }

    OpenApiVersion targetVersion() {
        return targetVersion;
    }

    DocumentPath path() {
        return path;
    }

    void error(DiagnosticCode code, String message) {
        add(code, DiagnosticSeverity.ERROR, message);
    }

    void warning(DiagnosticCode code, String message) {
        add(code, DiagnosticSeverity.WARNING, message);
    }

    boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(diagnostic ->
                        diagnostic.severity() == DiagnosticSeverity.ERROR);
    }

    List<OpenApiDiagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    private void add(
            DiagnosticCode code,
            DiagnosticSeverity severity,
            String message) {
        diagnostics.add(new OpenApiDiagnostic(
                code,
                severity,
                DiagnosticPhase.VERSION_COMPATIBILITY,
                message,
                path));
    }
}
