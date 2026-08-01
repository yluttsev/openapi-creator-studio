package ru.luttsev.studio.openapi.validation;

import java.util.List;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

@FunctionalInterface
public interface OpenApiSemanticValidator {

    List<OpenApiDiagnostic> validate(OpenApiDocument document);
}
