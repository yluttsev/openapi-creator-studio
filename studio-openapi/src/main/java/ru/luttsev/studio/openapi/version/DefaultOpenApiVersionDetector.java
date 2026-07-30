package ru.luttsev.studio.openapi.version;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.result.DetectedVersion;
import ru.luttsev.studio.openapi.result.VersionDetectionFailure;
import ru.luttsev.studio.openapi.result.VersionDetectionResult;

public final class DefaultOpenApiVersionDetector
        implements OpenApiVersionDetector {

    private static final String VERSION_FIELD = "openapi";
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)");
    private static final DocumentPath VERSION_PATH =
            DocumentPath.root().child(VERSION_FIELD);

    @Override
    public VersionDetectionResult detect(ObjectValue document) {
        Objects.requireNonNull(document, "document must not be null");

        DocumentValue versionValue = document.values().get(VERSION_FIELD);
        if (versionValue == null) {
            return failure(
                    OpenApiDiagnosticCodes.MISSING_VERSION,
                    "The OpenAPI version field is required");
        }
        if (!(versionValue instanceof StringValue stringValue)) {
            return failure(
                    OpenApiDiagnosticCodes.INVALID_VERSION_TYPE,
                    "The OpenAPI version must be a string");
        }
        if (!VERSION_PATTERN.matcher(stringValue.value()).matches()) {
            return failure(
                    OpenApiDiagnosticCodes.INVALID_VERSION,
                    "The OpenAPI version must use major.minor.patch format");
        }

        return new DetectedVersion(
                new OpenApiVersion(stringValue.value()),
                List.of());
    }

    private static VersionDetectionFailure failure(
            DiagnosticCode code,
            String message) {
        return new VersionDetectionFailure(List.of(new OpenApiDiagnostic(
                code,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.VERSION_DETECTION,
                message,
                VERSION_PATH)));
    }
}
