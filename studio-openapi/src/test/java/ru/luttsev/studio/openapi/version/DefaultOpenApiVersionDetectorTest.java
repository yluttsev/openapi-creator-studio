package ru.luttsev.studio.openapi.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.result.DetectedVersion;
import ru.luttsev.studio.openapi.result.VersionDetectionFailure;
import ru.luttsev.studio.openapi.result.VersionDetectionResult;

class DefaultOpenApiVersionDetectorTest {

    private final OpenApiVersionDetector detector =
            new DefaultOpenApiVersionDetector();

    @ParameterizedTest
    @ValueSource(strings = {
        "3.0.4",
        "3.1.2",
        "3.2.0",
        "4.0.0",
        "0.0.1"
    })
    void detectsAnyVersionWithValidFormat(String value) {
        ObjectValue document = documentWithVersion(new StringValue(value));

        DetectedVersion result = assertInstanceOf(
                DetectedVersion.class,
                detector.detect(document));

        assertEquals(value, result.version().value());
        assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    void reportsMissingVersion() {
        VersionDetectionResult result = detector.detect(
                new ObjectValue(Map.of()));

        assertFailure(
                result,
                OpenApiDiagnosticCodes.MISSING_VERSION);
    }

    @Test
    void reportsNonStringVersion() {
        ObjectValue document = documentWithVersion(
                new NumberValue(new BigDecimal("3.1")));

        VersionDetectionResult result = detector.detect(document);

        assertFailure(
                result,
                OpenApiDiagnosticCodes.INVALID_VERSION_TYPE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "3.1",
        "3.1.0.0",
        "03.1.0",
        "3.01.0",
        "3.1.00",
        "3.1.x",
        " 3.1.0",
        "3.1.0 ",
        "3.1.0-rc1"
    })
    void reportsInvalidVersionFormat(String value) {
        ObjectValue document = documentWithVersion(new StringValue(value));

        VersionDetectionResult result = detector.detect(document);

        assertFailure(
                result,
                OpenApiDiagnosticCodes.INVALID_VERSION);
    }

    @Test
    void rejectsNullDocument() {
        assertThrows(
                NullPointerException.class,
                () -> detector.detect(null));
    }

    private static ObjectValue documentWithVersion(
            DocumentValue version) {
        return new ObjectValue(Map.of("openapi", version));
    }

    private static void assertFailure(
            VersionDetectionResult result,
            DiagnosticCode expectedCode) {
        VersionDetectionFailure failure = assertInstanceOf(
                VersionDetectionFailure.class,
                result);
        assertEquals(1, failure.diagnostics().size());

        OpenApiDiagnostic diagnostic = failure.diagnostics().getFirst();
        assertEquals(expectedCode, diagnostic.code());
        assertEquals(DiagnosticPhase.VERSION_DETECTION, diagnostic.phase());
        assertEquals(
                DocumentPath.root().child("openapi"),
                diagnostic.path());
        assertTrue(diagnostic.sourcePosition().isEmpty());
    }
}
