package ru.luttsev.studio.openapi.validation.schema;

import ru.luttsev.studio.core.model.OpenApiVersion;

public interface OpenApiStructuralSchemaProvider {

    boolean supports(OpenApiVersion version);

    StructuralSchemaBundle schema();
}
