package ru.luttsev.studio.core.validation.support;

import java.util.Objects;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.navigation.DocumentPath;

public record ParameterOccurrence(
        Parameter parameter,
        DocumentPath usagePath,
        DocumentPath definitionPath) {

    public ParameterOccurrence {
        Objects.requireNonNull(parameter, "parameter must not be null");
        Objects.requireNonNull(usagePath, "usagePath must not be null");
        Objects.requireNonNull(definitionPath, "definitionPath must not be null");
    }

    public ParameterKey key() {
        return new ParameterKey(
                parameter.getName(),
                parameter.getLocation());
    }
}
