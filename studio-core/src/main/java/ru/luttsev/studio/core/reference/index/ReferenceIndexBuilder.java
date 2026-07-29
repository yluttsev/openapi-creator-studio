package ru.luttsev.studio.core.reference.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.reference.ReferenceResolution;
import ru.luttsev.studio.core.reference.ReferenceResolver;
import ru.luttsev.studio.core.reference.ResolvedReference;
import ru.luttsev.studio.core.reference.UnresolvedReference;

public final class ReferenceIndexBuilder {

    private static final String REFERENCE_FIELD = "$ref";

    private final DocumentNavigator navigator;
    private final ReferenceResolver resolver;

    public ReferenceIndexBuilder() {
        this(new DocumentNavigator());
    }

    public ReferenceIndexBuilder(DocumentNavigator navigator) {
        this.navigator = Objects.requireNonNull(
                navigator,
                "navigator must not be null");
        resolver = new ReferenceResolver(navigator);
    }

    public ReferenceIndex build(OpenApiDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        ArrayList<ReferenceUsage> references = new ArrayList<>();
        navigator.walk(document)
                .filter(ReferenceIndexBuilder::isReference)
                .forEach(entry -> references.add(index(document, entry)));
        return new ReferenceIndex(references);
    }

    private ReferenceUsage index(
            OpenApiDocument document,
            DocumentEntry entry) {
        UriReference reference = (UriReference) entry.value();
        ReferenceResolution<Object> resolution = resolver.resolve(document, reference);
        if (resolution instanceof ResolvedReference<Object> resolvedReference) {
            return new ResolvedReferenceUsage(
                    entry.path(),
                    reference,
                    resolvedReference.targetPath());
        }

        UnresolvedReference<Object> unresolvedReference =
                (UnresolvedReference<Object>) resolution;
        return new UnresolvedReferenceUsage(
                entry.path(),
                reference,
                unresolvedReference.failure());
    }

    private static boolean isReference(DocumentEntry entry) {
        if (!(entry.value() instanceof UriReference)) {
            return false;
        }

        List<String> segments = entry.path().segments();
        return !segments.isEmpty()
                && REFERENCE_FIELD.equals(segments.getLast());
    }
}
