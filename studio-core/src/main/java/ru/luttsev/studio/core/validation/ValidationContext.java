package ru.luttsev.studio.core.validation;

import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.reference.ReferenceResolver;

public record ValidationContext(
        OpenApiDocument document,
        DocumentNavigator navigator,
        ReferenceResolver referenceResolver) {

    public ValidationContext {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(navigator, "navigator must not be null");
        Objects.requireNonNull(referenceResolver, "referenceResolver must not be null");
    }
}
