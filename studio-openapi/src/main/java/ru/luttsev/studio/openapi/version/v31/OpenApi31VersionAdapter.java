package ru.luttsev.studio.openapi.version.v31;

import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapter;
import ru.luttsev.studio.openapi.version.OpenApiVersionFamily;
import ru.luttsev.studio.openapi.version.v31.decode.OpenApi31Decoder;
import ru.luttsev.studio.openapi.version.v31.encode.OpenApi31Encoder;

public final class OpenApi31VersionAdapter
        implements OpenApiVersionAdapter {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();
    private final OpenApi31Encoder encoder = new OpenApi31Encoder();

    @Override
    public boolean supports(OpenApiVersion version) {
        return OpenApiVersionFamily.V3_1.supports(version);
    }

    @Override
    public AdapterResult<OpenApiDocument> decode(
            ObjectValue source,
            OpenApiVersion version) {
        return decoder.decode(source, version);
    }

    @Override
    public AdapterResult<ObjectValue> encode(
            OpenApiDocument document,
            OpenApiVersion targetVersion) {
        return encoder.encode(document, targetVersion);
    }
}
