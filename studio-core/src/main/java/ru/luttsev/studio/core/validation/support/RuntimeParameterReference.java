package ru.luttsev.studio.core.validation.support;

import java.util.Objects;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;

public record RuntimeParameterReference(
        ParameterLocation location,
        String name) {

    public RuntimeParameterReference {
        Objects.requireNonNull(location, "location must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
