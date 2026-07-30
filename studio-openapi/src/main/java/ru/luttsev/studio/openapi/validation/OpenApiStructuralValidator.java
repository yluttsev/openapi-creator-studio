package ru.luttsev.studio.openapi.validation;

import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.result.StructuralValidationResult;

@FunctionalInterface
public interface OpenApiStructuralValidator {

    StructuralValidationResult validate(
            ObjectValue document,
            OpenApiVersion version);
}
