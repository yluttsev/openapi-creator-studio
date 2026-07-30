package ru.luttsev.studio.openapi.version;

import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.result.AdapterResult;

final class TestVersionAdapter implements OpenApiVersionAdapter {

    private final OpenApiVersion supportedVersion;

    TestVersionAdapter(OpenApiVersion supportedVersion) {
        this.supportedVersion = supportedVersion;
    }

    @Override
    public boolean supports(OpenApiVersion version) {
        return supportedVersion.equals(version);
    }

    @Override
    public AdapterResult<OpenApiDocument> decode(
            ObjectValue source,
            OpenApiVersion version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AdapterResult<ObjectValue> encode(
            OpenApiDocument document,
            OpenApiVersion targetVersion) {
        throw new UnsupportedOperationException();
    }
}
