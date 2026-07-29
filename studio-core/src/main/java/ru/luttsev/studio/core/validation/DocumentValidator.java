package ru.luttsev.studio.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.ReferenceResolver;
import ru.luttsev.studio.core.validation.profile.OpenApi31ValidationRulesProvider;

public final class DocumentValidator {

    private static final ValidationCode MISSING_VERSION =
            new ValidationCode("document.openapi-version.missing");
    private static final ValidationCode UNSUPPORTED_VERSION =
            new ValidationCode("document.openapi-version.unsupported");

    private final ValidationRulesProvider rulesProvider;
    private final DocumentNavigator navigator;
    private final ReferenceResolver referenceResolver;

    public DocumentValidator() {
        this(new OpenApi31ValidationRulesProvider());
    }

    public DocumentValidator(ValidationRulesProvider rulesProvider) {
        this(rulesProvider, new DocumentNavigator());
    }

    public DocumentValidator(
            ValidationRulesProvider rulesProvider,
            DocumentNavigator navigator) {
        this.rulesProvider = Objects.requireNonNull(
                rulesProvider,
                "rulesProvider must not be null");
        this.navigator = Objects.requireNonNull(
                navigator,
                "navigator must not be null");
        this.referenceResolver = new ReferenceResolver(navigator);
    }

    public ValidationResult validate(OpenApiDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        if (document.getOpenApiVersion() == null) {
            return new ValidationResult(List.of(new ValidationIssue(
                    MISSING_VERSION,
                    ValidationSeverity.ERROR,
                    "OpenAPI version is required",
                    DocumentPath.root().child("openapi"))));
        }

        List<ValidationRule> rules =
                rulesProvider.rulesFor(document.getOpenApiVersion());
        if (rules.isEmpty()) {
            return new ValidationResult(List.of(new ValidationIssue(
                    UNSUPPORTED_VERSION,
                    ValidationSeverity.ERROR,
                    "OpenAPI version is not supported by this validator: "
                            + document.getOpenApiVersion().value(),
                    DocumentPath.root().child("openapi"))));
        }

        ValidationContext context =
                new ValidationContext(document, navigator, referenceResolver);
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (ValidationRule rule : rules) {
            issues.addAll(rule.validate(context));
        }
        return new ValidationResult(issues);
    }
}
