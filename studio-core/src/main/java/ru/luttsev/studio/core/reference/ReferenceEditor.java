package ru.luttsev.studio.core.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.reference.ReferenceHolder;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentNavigator;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.index.ReferenceIndex;
import ru.luttsev.studio.core.reference.index.ResolvedReferenceUsage;

public final class ReferenceEditor {

    private final DocumentNavigator navigator;

    public ReferenceEditor() {
        this(new DocumentNavigator());
    }

    public ReferenceEditor(DocumentNavigator navigator) {
        this.navigator = Objects.requireNonNull(
                navigator,
                "navigator must not be null");
    }

    public List<DocumentPath> replaceTargetPrefix(
            OpenApiDocument document,
            ReferenceIndex index,
            DocumentPath currentTarget,
            DocumentPath newTarget) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(index, "index must not be null");
        Objects.requireNonNull(currentTarget, "currentTarget must not be null");
        Objects.requireNonNull(newTarget, "newTarget must not be null");

        List<ResolvedReferenceUsage> usages = index.usagesOfSubtree(currentTarget);
        for (ResolvedReferenceUsage usage : usages) {
            findHolder(document, usage.sourcePath());
        }

        ArrayList<DocumentPath> changedPaths = new ArrayList<>();
        for (ResolvedReferenceUsage usage : usages) {
            ReferenceHolder holder = findHolder(document, usage.sourcePath());
            DocumentPath replacementTarget = usage.targetPath().replacePrefix(
                    currentTarget,
                    newTarget);
            holder.setRef(new UriReference(replacementTarget.toFragment()));
            changedPaths.add(usage.sourcePath());
        }
        return List.copyOf(changedPaths);
    }

    private ReferenceHolder findHolder(
            OpenApiDocument document,
            DocumentPath referencePath) {
        DocumentPath ownerPath = referencePath.parent().orElseThrow(
                () -> new IllegalStateException(
                        "Reference path has no owning object: " + referencePath));
        return navigator.find(document, ownerPath, ReferenceHolder.class)
                .orElseThrow(() -> new IllegalStateException(
                        "Reference index points to a non-reference object: "
                                + referencePath));
    }
}
