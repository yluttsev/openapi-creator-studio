package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class EncodeContext {

    private final OpenApiVersion targetVersion;
    private final EncodeContext parent;
    private final String segment;
    private final List<OpenApiDiagnostic> diagnostics;
    private final Set<Object> activeObjects;

    private EncodeContext(
            OpenApiVersion targetVersion,
            EncodeContext parent,
            String segment,
            List<OpenApiDiagnostic> diagnostics,
            Set<Object> activeObjects) {
        this.targetVersion = targetVersion;
        this.parent = parent;
        this.segment = segment;
        this.diagnostics = diagnostics;
        this.activeObjects = activeObjects;
    }

    static EncodeContext root(OpenApiVersion targetVersion) {
        Objects.requireNonNull(
                targetVersion,
                "targetVersion must not be null");
        return new EncodeContext(
                targetVersion,
                null,
                null,
                new ArrayList<>(),
                Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    EncodeContext child(String segment) {
        Objects.requireNonNull(segment, "segment must not be null");
        return new EncodeContext(
                targetVersion,
                this,
                segment,
                diagnostics,
                activeObjects);
    }

    OpenApiVersion targetVersion() {
        return targetVersion;
    }

    DocumentPath path() {
        ArrayDeque<String> segments = new ArrayDeque<>();
        EncodeContext current = this;
        while (current.parent != null) {
            segments.addFirst(current.segment);
            current = current.parent;
        }

        StringBuilder pointer = new StringBuilder();
        for (String pathSegment : segments) {
            pointer.append('/')
                    .append(pathSegment
                            .replace("~", "~0")
                            .replace("/", "~1"));
        }
        return DocumentPath.parse(pointer.toString());
    }

    void error(DiagnosticCode code, String message) {
        add(
                code,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.VERSION_COMPATIBILITY,
                message);
    }

    void warning(DiagnosticCode code, String message) {
        add(
                code,
                DiagnosticSeverity.WARNING,
                DiagnosticPhase.VERSION_COMPATIBILITY,
                message);
    }

    void mappingError(DiagnosticCode code, String message) {
        add(code, DiagnosticSeverity.ERROR, DiagnosticPhase.MAPPING, message);
    }

    boolean enter(Object value) {
        return activeObjects.add(Objects.requireNonNull(
                value,
                "value must not be null"));
    }

    void leave(Object value) {
        activeObjects.remove(Objects.requireNonNull(
                value,
                "value must not be null"));
    }

    void unsupportedField(String field) {
        child(field).error(
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "Field '" + field + "' is not supported by OpenAPI "
                        + targetVersion.value());
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
            DiagnosticPhase phase,
            String message) {
        diagnostics.add(new OpenApiDiagnostic(
                code,
                severity,
                phase,
                message,
                path()));
    }
}
