package ru.luttsev.studio.core.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.navigation.DocumentPath;

class ValidationInfrastructureTest {

    @Test
    void validatesAnEmptyOpenApi31Document() {
        ValidationResult result =
                new DocumentValidator().validate(ValidationFixture.document());

        assertTrue(result.isValid());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void reportsMissingAndUnsupportedVersions() {
        OpenApiDocument missingVersion = ValidationFixture.document();
        missingVersion.setOpenApiVersion(null);
        OpenApiDocument unsupportedVersion = ValidationFixture.document();
        unsupportedVersion.setOpenApiVersion(OpenApiVersion.V3_0_4);

        ValidationResult missingResult =
                new DocumentValidator().validate(missingVersion);
        ValidationResult unsupportedResult =
                new DocumentValidator().validate(unsupportedVersion);

        assertEquals(
                "document.openapi-version.missing",
                missingResult.errors().getFirst().code().value());
        assertEquals(
                "document.openapi-version.unsupported",
                unsupportedResult.errors().getFirst().code().value());
    }

    @Test
    void validationResultIsImmutableAndWarningsRemainValid() {
        ArrayList<ValidationIssue> source = new ArrayList<>();
        source.add(new ValidationIssue(
                new ValidationCode("test.warning"),
                ValidationSeverity.WARNING,
                "warning",
                DocumentPath.root()));

        ValidationResult result = new ValidationResult(source);
        source.clear();

        assertTrue(result.isValid());
        assertEquals(1, result.warnings().size());
        assertTrue(result.errors().isEmpty());
        assertThrows(
                UnsupportedOperationException.class,
                () -> result.issues().add(new ValidationIssue(
                        new ValidationCode("other"),
                        ValidationSeverity.ERROR,
                        "other",
                        DocumentPath.root())));

        ValidationResult invalid = new ValidationResult(List.of(new ValidationIssue(
                new ValidationCode("test.error"),
                ValidationSeverity.ERROR,
                "error",
                DocumentPath.root())));
        assertFalse(invalid.isValid());
    }
}
