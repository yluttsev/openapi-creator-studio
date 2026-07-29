package ru.luttsev.studio.core.reference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.navigation.DocumentPath;

public final class ReferenceResolver {

    private final DocumentNavigator navigator;

    public ReferenceResolver() {
        this(new DocumentNavigator());
    }

    public ReferenceResolver(DocumentNavigator navigator) {
        this.navigator = Objects.requireNonNull(
                navigator,
                "navigator must not be null");
    }

    public ReferenceResolution<Object> resolve(
            OpenApiDocument document,
            UriReference reference) {
        return resolve(document, reference, Object.class);
    }

    public <T> ReferenceResolution<T> resolve(
            OpenApiDocument document,
            UriReference reference,
            Class<T> expectedType) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(reference, "reference must not be null");
        Objects.requireNonNull(expectedType, "expectedType must not be null");

        ParsedReference parsedReference = parse(reference);
        if (parsedReference instanceof RejectedReference rejectedReference) {
            return new UnresolvedReference<>(
                    reference,
                    rejectedReference.failure());
        }

        DocumentPath targetPath = ((LocalPointer) parsedReference).path();
        Optional<Object> target = navigator.find(document, targetPath);
        if (target.isEmpty()) {
            return new UnresolvedReference<>(
                    reference,
                    ReferenceFailure.TARGET_NOT_FOUND);
        }

        Object value = target.orElseThrow();
        if (!expectedType.isInstance(value)) {
            return new UnresolvedReference<>(
                    reference,
                    ReferenceFailure.TYPE_MISMATCH);
        }
        return new ResolvedReference<>(
                expectedType.cast(value),
                targetPath);
    }

    private static ParsedReference parse(UriReference reference) {
        String value = reference.value();
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException exception) {
            return new RejectedReference(ReferenceFailure.INVALID_REFERENCE);
        }

        if (isExternal(uri)) {
            return new RejectedReference(ReferenceFailure.EXTERNAL_REFERENCE);
        }
        if (value.isEmpty()) {
            return new LocalPointer(DocumentPath.root());
        }

        String fragment = uri.getFragment();
        if (fragment == null) {
            return new RejectedReference(ReferenceFailure.INVALID_REFERENCE);
        }
        if (!fragment.isEmpty() && !fragment.startsWith("/")) {
            return new RejectedReference(ReferenceFailure.UNSUPPORTED_ANCHOR);
        }

        try {
            return new LocalPointer(DocumentPath.parseFragment(value));
        } catch (IllegalArgumentException exception) {
            return new RejectedReference(ReferenceFailure.INVALID_REFERENCE);
        }
    }

    private static boolean isExternal(URI uri) {
        return uri.getScheme() != null
                || uri.getAuthority() != null
                || uri.getQuery() != null
                || uri.getPath() != null && !uri.getPath().isEmpty();
    }

}
