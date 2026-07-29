package ru.luttsev.studio.core.reference;

import java.util.Objects;

record RejectedReference(ReferenceFailure failure) implements ParsedReference {

    RejectedReference {
        Objects.requireNonNull(failure, "failure must not be null");
    }
}
