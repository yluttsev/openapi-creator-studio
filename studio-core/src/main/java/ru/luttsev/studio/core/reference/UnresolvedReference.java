package ru.luttsev.studio.core.reference;

import java.util.Objects;
import ru.luttsev.studio.core.model.schema.UriReference;

public record UnresolvedReference<T>(
        UriReference reference,
        ReferenceFailure failure)
        implements ReferenceResolution<T> {

    public UnresolvedReference {
        Objects.requireNonNull(reference, "reference must not be null");
        Objects.requireNonNull(failure, "failure must not be null");
    }
}
