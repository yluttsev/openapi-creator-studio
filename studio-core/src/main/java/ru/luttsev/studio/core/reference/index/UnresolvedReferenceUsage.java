package ru.luttsev.studio.core.reference.index;

import java.util.Objects;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.ReferenceFailure;

public record UnresolvedReferenceUsage(
        DocumentPath sourcePath,
        UriReference reference,
        ReferenceFailure failure)
        implements ReferenceUsage {

    public UnresolvedReferenceUsage {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");
        Objects.requireNonNull(reference, "reference must not be null");
        Objects.requireNonNull(failure, "failure must not be null");
    }
}
