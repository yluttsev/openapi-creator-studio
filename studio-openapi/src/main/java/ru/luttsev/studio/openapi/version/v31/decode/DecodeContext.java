package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

final class DecodeContext {

    private final OpenApiVersion version;
    private final DocumentPath path;
    private final DiagnosticCollector diagnosticCollector;

    private DecodeContext(
            OpenApiVersion version,
            DocumentPath path,
            DiagnosticCollector diagnosticCollector) {
        this.version = version;
        this.path = path;
        this.diagnosticCollector = diagnosticCollector;
    }

    static DecodeContext root(OpenApiVersion version) {
        Objects.requireNonNull(version, "version must not be null");
        return new DecodeContext(
                version,
                DocumentPath.root(),
                new DiagnosticCollector());
    }

    DecodeContext child(String segment) {
        Objects.requireNonNull(segment, "segment must not be null");
        return new DecodeContext(
                version,
                path.child(segment),
                diagnosticCollector);
    }

    OpenApiVersion version() {
        return version;
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
        return diagnosticCollector.hasErrors();
    }

    List<OpenApiDiagnostic> diagnostics() {
        return diagnosticCollector.diagnostics();
    }

    private void add(
            DiagnosticCode code,
            DiagnosticSeverity severity,
            String message) {
        diagnosticCollector.add(new OpenApiDiagnostic(
                code,
                severity,
                DiagnosticPhase.MAPPING,
                message,
                path));
    }
}
