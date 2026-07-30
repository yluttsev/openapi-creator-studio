package ru.luttsev.studio.openapi.version;

import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.result.AdapterResult;

public interface OpenApiVersionAdapter {

    boolean supports(OpenApiVersion version);

    AdapterResult<OpenApiDocument> decode(
            ObjectValue source,
            OpenApiVersion version);

    AdapterResult<ObjectValue> encode(
            OpenApiDocument document,
            OpenApiVersion targetVersion);
}
