package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ReferenceOrDecoder {

    private final ReferenceObjectDecoder referenceObjectDecoder =
            new ReferenceObjectDecoder();

    <T> ReferenceOr<T> decode(
            DocumentValue source,
            DecodeContext context,
            BiFunction<ObjectValue, DecodeContext, T> inlineDecoder) {
        if (!(source instanceof ObjectValue objectValue)) {
            context.error(
                    OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                    "Expected object but found "
                            + ObjectValueReader.typeOf(source));
            return null;
        }
        if (objectValue.values().containsKey("$ref")) {
            return referenceObjectDecoder.decode(objectValue, context);
        }

        T value = inlineDecoder.apply(objectValue, context);
        return value == null ? null : new InlineObject<>(value);
    }

    <T> Map<String, ReferenceOr<T>> decodeMap(
            ObjectValue source,
            DecodeContext context,
            BiFunction<ObjectValue, DecodeContext, T> inlineDecoder) {
        return ObjectValueMapper.mapValues(
                source,
                context,
                (value, itemContext) ->
                        decode(value, itemContext, inlineDecoder));
    }
}
