package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class CallbackDecoder {

    private static final String EXTENSION_PREFIX = "x-";

    private final ReferenceOrDecoder referenceOrDecoder =
            new ReferenceOrDecoder();

    Map<String, ReferenceOr<Callback>> decodeMap(
            ObjectValue source,
            DecodeContext context,
            BiFunction<ObjectValue, DecodeContext, PathItem> pathItemDecoder) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                (object, itemContext) ->
                        decode(object, itemContext, pathItemDecoder));
    }

    private static Callback decode(
            ObjectValue source,
            DecodeContext context,
            BiFunction<ObjectValue, DecodeContext, PathItem> pathItemDecoder) {
        Callback callback = new Callback();
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (entry.getKey().startsWith(EXTENSION_PREFIX)) {
                callback.getAdditionalFields().put(
                        entry.getKey(),
                        entry.getValue());
                continue;
            }

            DecodeContext expressionContext = context.child(entry.getKey());
            if (entry.getValue() instanceof ObjectValue objectValue) {
                callback.getExpressions().put(
                        entry.getKey(),
                        pathItemDecoder.apply(objectValue, expressionContext));
            } else {
                expressionContext.error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected object but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
        return callback;
    }
}
