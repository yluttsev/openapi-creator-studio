package ru.luttsev.studio.core.reference.index;

import java.util.Objects;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record ResolvedReferenceUsage(
        DocumentPath sourcePath,
        UriReference reference,
        DocumentPath targetPath)
        implements ReferenceUsage {

    public ResolvedReferenceUsage {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(reference, "reference must not be null");
        Objects.requireNonNull(targetPath, "targetPath must not be null");
    }
}
