package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class PathsDecoder {

    private final PathItemDecoder pathItemDecoder = new PathItemDecoder(
            new PayloadDecoder(new SchemaDecoder()));

    Paths decode(ObjectValue source, DecodeContext context) {
        Paths paths = new Paths();
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (!entry.getKey().startsWith("/")) {
                paths.getAdditionalFields().put(
                        entry.getKey(),
                        entry.getValue());
                continue;
            }

            DecodeContext pathContext = context.child(entry.getKey());
            if (entry.getValue() instanceof ObjectValue objectValue) {
                PathItem pathItem = pathItemDecoder.decode(
                        objectValue,
                        pathContext);
                paths.getItems().put(entry.getKey(), pathItem);
            } else {
                pathContext.error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected object but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
        return paths;
    }
}
