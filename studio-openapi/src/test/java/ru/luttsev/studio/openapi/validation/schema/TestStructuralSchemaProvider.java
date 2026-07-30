package ru.luttsev.studio.openapi.validation.schema;

import ru.luttsev.studio.core.model.OpenApiVersion;

final class TestStructuralSchemaProvider
        implements OpenApiStructuralSchemaProvider {

    private final OpenApiVersion supportedVersion;
    private final StructuralSchemaBundle schema;

    TestStructuralSchemaProvider(
            OpenApiVersion supportedVersion,
            StructuralSchemaBundle schema) {
        this.supportedVersion = supportedVersion;
        this.schema = schema;
    }

    @Override
    public boolean supports(OpenApiVersion version) {
        return supportedVersion.equals(version);
    }

    @Override
    public StructuralSchemaBundle schema() {
        return schema;
    }
}
