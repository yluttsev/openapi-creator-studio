package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.reference.ReferenceFailure;
import ru.luttsev.studio.core.reference.ReferenceResolution;
import ru.luttsev.studio.core.reference.ResolvedReference;
import ru.luttsev.studio.core.reference.UnresolvedReference;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.ReferenceExpectedTypeResolver;
import ru.luttsev.studio.core.validation.support.ReferenceValueResolver;

public final class ReferenceIntegrityRule implements ValidationRule {

    private static final ValidationCode INVALID_REFERENCE =
            new ValidationCode("reference.invalid");
    private static final ValidationCode TARGET_NOT_FOUND =
            new ValidationCode("reference.target.not-found");
    private static final ValidationCode TYPE_MISMATCH =
            new ValidationCode("reference.target.type-mismatch");
    private static final ValidationCode UNCHECKED_REFERENCE =
            new ValidationCode("reference.target.unchecked");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        for (DocumentEntry entry : DocumentEntries.ofType(
                context,
                ReferenceObject.class)) {
            ReferenceObject<?> reference = (ReferenceObject<?>) entry.value();
            if (reference.getRef() == null) {
                continue;
            }
            Optional<Class<?>> expectedType =
                    ReferenceExpectedTypeResolver.resolve(context, entry.path());
            validateReference(
                    context,
                    reference.getRef(),
                    expectedType,
                    entry.path().child("$ref"),
                    issues);
        }
        for (DocumentEntry entry : DocumentEntries.ofType(
                context,
                SchemaDefinition.class)) {
            SchemaDefinition schema = (SchemaDefinition) entry.value();
            if (schema.getRef() != null) {
                validateReference(
                        context,
                        schema.getRef(),
                        Optional.of(Schema.class),
                        entry.path().child("$ref"),
                        issues);
            }
        }
        for (DocumentEntry entry : DocumentEntries.ofType(context, PathItem.class)) {
            PathItem pathItem = (PathItem) entry.value();
            if (pathItem.getRef() != null) {
                validateReference(
                        context,
                        pathItem.getRef(),
                        Optional.of(PathItem.class),
                        entry.path().child("$ref"),
                        issues);
            }
        }
        return List.copyOf(issues);
    }

    private static void validateReference(
            ValidationContext context,
            UriReference reference,
            Optional<Class<?>> expectedType,
            ru.luttsev.studio.core.navigation.DocumentPath path,
            List<ValidationIssue> issues) {
        ReferenceResolution<Object> resolution =
                context.referenceResolver().resolve(context.document(), reference);
        if (resolution instanceof UnresolvedReference<Object> unresolved) {
            issues.add(issueForFailure(unresolved.failure(), path));
            return;
        }

        if (expectedType.isEmpty()) {
            return;
        }
        Object target = ((ResolvedReference<Object>) resolution).value();
        Class<?> targetType = expectedType.orElseThrow();
        if (targetType.isInstance(target)
                || target instanceof ReferenceObject<?>) {
            return;
        }

        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        if (resolver.resolve(reference, targetType).isEmpty()) {
            issues.add(new ValidationIssue(
                    TYPE_MISMATCH,
                    ValidationSeverity.ERROR,
                    "Reference target has an unexpected type; expected "
                            + targetType.getSimpleName(),
                    path));
        }
    }

    private static ValidationIssue issueForFailure(
            ReferenceFailure failure,
            ru.luttsev.studio.core.navigation.DocumentPath path) {
        return switch (failure) {
            case EXTERNAL_REFERENCE, UNSUPPORTED_ANCHOR -> new ValidationIssue(
                    UNCHECKED_REFERENCE,
                    ValidationSeverity.WARNING,
                    "Reference cannot be checked by the local reference resolver",
                    path);
            case TARGET_NOT_FOUND -> new ValidationIssue(
                    TARGET_NOT_FOUND,
                    ValidationSeverity.ERROR,
                    "Reference target does not exist",
                    path);
            case TYPE_MISMATCH -> new ValidationIssue(
                    TYPE_MISMATCH,
                    ValidationSeverity.ERROR,
                    "Reference target has an unexpected type",
                    path);
            case INVALID_REFERENCE -> new ValidationIssue(
                    INVALID_REFERENCE,
                    ValidationSeverity.ERROR,
                    "Reference is not a valid local URI reference",
                    path);
        };
    }
}
