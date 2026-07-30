package ru.luttsev.studio.openapi.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.StructuralValidationFailure;
import ru.luttsev.studio.openapi.result.StructuralValidationResult;
import ru.luttsev.studio.openapi.result.StructuralValidationSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;

class DefaultOpenApiStructuralValidatorTest {

    private final OpenApiStructuralValidator validator =
            new DefaultOpenApiStructuralValidator();

    @Test
    void acceptsValidOpenApi31Document() {
        ObjectValue document = parse("""
                openapi: 3.1.2
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                """);

        StructuralValidationResult result =
                validator.validate(document, OpenApiVersion.V3_1_2);

        StructuralValidationSuccess success = assertInstanceOf(
                StructuralValidationSuccess.class,
                result);
        assertTrue(success.diagnostics().isEmpty());
    }

    @Test
    void usesSameSchemaForAllOpenApi31PatchVersions() {
        ObjectValue document = parse("""
                openapi: 3.1.0
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                """);

        assertInstanceOf(
                StructuralValidationSuccess.class,
                validator.validate(document, new OpenApiVersion("3.1.0")));
    }

    @Test
    void rejectsDocumentWithoutRequiredInfo() {
        ObjectValue document = parse("""
                openapi: 3.1.2
                paths: {}
                """);

        StructuralValidationFailure failure = assertInstanceOf(
                StructuralValidationFailure.class,
                validator.validate(document, OpenApiVersion.V3_1_2));

        assertStructuralErrors(failure);
        assertTrue(failure.diagnostics().stream()
                .map(diagnostic -> diagnostic.path().toPointer())
                .anyMatch(path -> path.equals("/info") || path.isEmpty()));
    }

    @Test
    void validatesSchemaObjectsWithOpenApiBaseDialect() {
        ObjectValue document = parse("""
                openapi: 3.1.2
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                components:
                  schemas:
                    User:
                      type: 123
                """);

        StructuralValidationFailure failure = assertInstanceOf(
                StructuralValidationFailure.class,
                validator.validate(document, OpenApiVersion.V3_1_2));

        assertStructuralErrors(failure);
        assertTrue(failure.diagnostics().stream()
                .map(diagnostic -> diagnostic.path().toPointer())
                .anyMatch(path ->
                        path.startsWith("/components/schemas/User")));
    }

    @Test
    void reportsUnsupportedOpenApiVersion() {
        ObjectValue document = parse("""
                openapi: 3.0.4
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                """);

        StructuralValidationFailure failure = assertInstanceOf(
                StructuralValidationFailure.class,
                validator.validate(document, OpenApiVersion.V3_0_4));
        OpenApiDiagnostic diagnostic = failure.diagnostics().getFirst();

        assertEquals(
                OpenApiDiagnosticCodes.UNSUPPORTED_STRUCTURE_VERSION,
                diagnostic.code());
        assertEquals(
                DiagnosticPhase.STRUCTURAL_VALIDATION,
                diagnostic.phase());
        assertEquals("/openapi", diagnostic.path().toPointer());
    }

    private static void assertStructuralErrors(
            StructuralValidationFailure failure) {
        assertFalse(failure.diagnostics().isEmpty());
        assertTrue(failure.diagnostics().stream()
                .allMatch(diagnostic ->
                        diagnostic.code().equals(
                                OpenApiDiagnosticCodes.INVALID_STRUCTURE)
                                && diagnostic.phase()
                                == DiagnosticPhase.STRUCTURAL_VALIDATION));
    }

    private static ObjectValue parse(String content) {
        JacksonOpenApiSyntaxCodec codec = new JacksonOpenApiSyntaxCodec();
        SyntaxSuccess<?> success = assertInstanceOf(
                SyntaxSuccess.class,
                codec.parse(content, ImportOptions.autoDetect()));
        ParsedDocument parsedDocument = assertInstanceOf(
                ParsedDocument.class,
                success.value());
        return parsedDocument.root();
    }
}
