package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.ReferenceFailure;
import ru.luttsev.studio.core.reference.index.ReferenceIndex;
import ru.luttsev.studio.core.reference.index.ReferenceIndexBuilder;
import ru.luttsev.studio.core.reference.index.ReferenceUsage;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;
import ru.luttsev.studio.core.reference.index.UnresolvedReferenceUsage;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
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
        ReferenceIndex index =
                new ReferenceIndexBuilder(context.navigator()).build(context.document());
        for (ReferenceUsage usage : index.references()) {
            if (usage instanceof UnresolvedReferenceUsage unresolvedReference) {
                issues.add(issueForFailure(
                        unresolvedReference.failure(),
                        unresolvedReference.sourcePath()));
                continue;
            }

            ResolvedReferenceUsage resolvedReference =
                    (ResolvedReferenceUsage) usage;
            validateResolvedReference(
                    context,
                    resolvedReference,
                    expectedType(context, resolvedReference.sourcePath()),
                    issues);
        }
        return List.copyOf(issues);
    }

    private static Optional<Class<?>> expectedType(
            ValidationContext context,
            DocumentPath referencePath) {
        Optional<DocumentPath> ownerPath = referencePath.parent();
        if (ownerPath.isEmpty()) {
            return Optional.empty();
        }

        Optional<Object> owner =
                context.navigator().find(context.document(), ownerPath.orElseThrow());
        if (owner.isEmpty()) {
            return Optional.empty();
        }

        Object ownerValue = owner.orElseThrow();
        if (ownerValue instanceof SchemaDefinition) {
            return Optional.of(Schema.class);
        }
        if (ownerValue instanceof PathItem) {
            return Optional.of(PathItem.class);
        }
        if (ownerValue instanceof ReferenceObject<?>) {
            return ReferenceExpectedTypeResolver.resolve(
                    context,
                    ownerPath.orElseThrow());
        }
        return Optional.empty();
    }

    private static void validateResolvedReference(
            ValidationContext context,
            ResolvedReferenceUsage reference,
            Optional<Class<?>> expectedType,
            List<ValidationIssue> issues) {
        if (expectedType.isEmpty()) {
            return;
        }

        Object target = context.navigator()
                .find(context.document(), reference.targetPath())
                .orElseThrow();
        Class<?> targetType = expectedType.orElseThrow();
        if (targetType.isInstance(target)
                || target instanceof ReferenceObject<?>) {
            return;
        }

        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        if (resolver.resolve(reference.reference(), targetType).isEmpty()) {
            issues.add(new ValidationIssue(
                    TYPE_MISMATCH,
                    ValidationSeverity.ERROR,
                    "Reference target has an unexpected type; expected "
                            + targetType.getSimpleName(),
                    reference.sourcePath()));
        }
    }

    private static ValidationIssue issueForFailure(
            ReferenceFailure failure,
            DocumentPath path) {
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
