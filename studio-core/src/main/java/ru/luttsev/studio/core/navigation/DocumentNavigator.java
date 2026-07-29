package ru.luttsev.studio.core.navigation;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.reference.InlineObject;

public final class DocumentNavigator {

    public Optional<Object> find(OpenApiDocument document, DocumentPath path) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(path, "path must not be null");

        Object current = document;
        for (String segment : path.segments()) {
            current = unwrapInlineObject(current);
            Map<String, Object> children = DocumentChildren.of(current);
            if (!children.containsKey(segment)) {
                return Optional.empty();
            }
            current = children.get(segment);
        }
        return Optional.of(unwrapInlineObject(current));
    }

    public <T> Optional<T> find(
            OpenApiDocument document,
            DocumentPath path,
            Class<T> expectedType) {
        Objects.requireNonNull(expectedType, "expectedType must not be null");
        return find(document, path)
                .filter(expectedType::isInstance)
                .map(expectedType::cast);
    }

    public Stream<DocumentEntry> walk(OpenApiDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        ArrayList<DocumentEntry> entries = new ArrayList<>();
        walk(
                DocumentPath.root(),
                document,
                entries,
                new IdentityHashMap<>());
        return entries.stream();
    }

    private void walk(
            DocumentPath path,
            Object value,
            ArrayList<DocumentEntry> entries,
            IdentityHashMap<Object, Boolean> ancestors) {
        Object unwrappedValue = unwrapInlineObject(value);
        entries.add(new DocumentEntry(path, unwrappedValue));

        if (ancestors.put(unwrappedValue, Boolean.TRUE) != null) {
            return;
        }

        for (Map.Entry<String, Object> child : DocumentChildren.of(unwrappedValue).entrySet()) {
            walk(
                    path.child(child.getKey()),
                    child.getValue(),
                    entries,
                    ancestors);
        }
        ancestors.remove(unwrappedValue);
    }

    private static Object unwrapInlineObject(Object value) {
        Object unwrapped = value;
        while (unwrapped instanceof InlineObject<?> inlineObject) {
            unwrapped = inlineObject.value();
        }
        return unwrapped;
    }
}
