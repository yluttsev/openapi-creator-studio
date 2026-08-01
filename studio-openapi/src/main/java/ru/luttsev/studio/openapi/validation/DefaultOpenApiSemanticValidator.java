package ru.luttsev.studio.openapi.validation;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.validation.DocumentValidator;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public final class DefaultOpenApiSemanticValidator
        implements OpenApiSemanticValidator {

    private final DocumentValidator documentValidator;

    public DefaultOpenApiSemanticValidator() {
        this(new DocumentValidator());
    }

    public DefaultOpenApiSemanticValidator(
            DocumentValidator documentValidator) {
        this.documentValidator = Objects.requireNonNull(
                documentValidator,
                "documentValidator must not be null");
    }

    @Override
    public List<OpenApiDiagnostic> validate(OpenApiDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        return documentValidator.validate(document).issues().stream()
                .map(DefaultOpenApiSemanticValidator::toDiagnostic)
                .toList();
    }

    private static OpenApiDiagnostic toDiagnostic(ValidationIssue issue) {
        return new OpenApiDiagnostic(
                new DiagnosticCode(issue.code().value()),
                severity(issue.severity()),
                DiagnosticPhase.SEMANTIC_VALIDATION,
                issue.message(),
                issue.path());
    }

    private static DiagnosticSeverity severity(
            ValidationSeverity severity) {
        return switch (severity) {
            case ERROR -> DiagnosticSeverity.ERROR;
            case WARNING -> DiagnosticSeverity.WARNING;
        };
    }
}
