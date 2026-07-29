package ru.luttsev.studio.core.command;

import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.reference.ReferenceEditor;
import ru.luttsev.studio.core.reference.index.ReferenceIndex;

public record CommandContext(
        OpenApiDocument document,
        DocumentNavigator navigator,
        ReferenceIndex referenceIndex,
        ReferenceEditor referenceEditor) {

    public CommandContext {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(navigator, "navigator must not be null");
        Objects.requireNonNull(referenceIndex, "referenceIndex must not be null");
        Objects.requireNonNull(referenceEditor, "referenceEditor must not be null");
    }
}
