package ru.luttsev.studio.core.validation.support;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.ReferenceResolution;
import ru.luttsev.studio.core.reference.ResolvedReference;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class ReferenceValueResolver {

    private final ValidationContext context;

    public ReferenceValueResolver(ValidationContext context) {
        this.context = Objects.requireNonNull(
                context,
                "context must not be null");
    }

    public <T> Optional<T> resolve(
            ReferenceOr<T> referenceOr,
            Class<T> expectedType) {
        return resolveWithPath(referenceOr, expectedType).map(ResolvedValue::value);
    }

    public <T> Optional<ResolvedValue<T>> resolveWithPath(
            ReferenceOr<T> referenceOr,
            Class<T> expectedType) {
        return resolveWithPath(
                referenceOr,
                expectedType,
                DocumentPath.root());
    }

    public <T> Optional<ResolvedValue<T>> resolveWithPath(
            ReferenceOr<T> referenceOr,
            Class<T> expectedType,
            DocumentPath inlinePath) {
        if (referenceOr instanceof InlineObject<T> inlineObject) {
            return Optional.of(new ResolvedValue<>(
                    inlineObject.value(),
                    inlinePath));
        }
        ReferenceObject<T> referenceObject =
                (ReferenceObject<T>) referenceOr;
        if (referenceObject.getRef() == null) {
            return Optional.empty();
        }
        return resolveWithPath(referenceObject.getRef(), expectedType);
    }

    public <T> Optional<T> resolve(
            UriReference reference,
            Class<T> expectedType) {
        return resolveWithPath(reference, expectedType).map(ResolvedValue::value);
    }

    public <T> Optional<ResolvedValue<T>> resolveWithPath(
            UriReference reference,
            Class<T> expectedType) {
        return resolveWithPath(reference, expectedType, new HashSet<>());
    }

    private <T> Optional<ResolvedValue<T>> resolveWithPath(
            UriReference reference,
            Class<T> expectedType,
            Set<DocumentPath> visitedPaths) {
        ReferenceResolution<Object> resolution =
                context.referenceResolver().resolve(context.document(), reference);
        if (!(resolution instanceof ResolvedReference<Object> resolvedReference)) {
            return Optional.empty();
        }
        if (!visitedPaths.add(resolvedReference.targetPath())) {
            return Optional.empty();
        }

        Object value = resolvedReference.value();
        if (expectedType.isInstance(value)) {
            return Optional.of(new ResolvedValue<>(
                    expectedType.cast(value),
                    resolvedReference.targetPath()));
        }
        if (value instanceof ReferenceObject<?> referenceObject
                && referenceObject.getRef() != null) {
            return resolveWithPath(
                    referenceObject.getRef(),
                    expectedType,
                    visitedPaths);
        }
        return Optional.empty();
    }
}
