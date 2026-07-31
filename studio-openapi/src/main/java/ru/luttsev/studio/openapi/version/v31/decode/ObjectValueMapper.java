package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ObjectValueMapper {

    private ObjectValueMapper() {
    }

    static <T> Map<String, T> mapObjects(
            ObjectValue source,
            DecodeContext context,
            BiFunction<ObjectValue, DecodeContext, T> mapper) {
        return mapValues(source, context, (value, itemContext) -> {
            if (value instanceof ObjectValue objectValue) {
                return mapper.apply(objectValue, itemContext);
            }
            itemContext.error(
                    OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                    "Expected object but found "
                            + ObjectValueReader.typeOf(value));
            return null;
        });
    }

    static <T> Map<String, T> mapValues(
            ObjectValue source,
            DecodeContext context,
            BiFunction<DocumentValue, DecodeContext, T> mapper) {
        LinkedHashMap<String, T> values = new LinkedHashMap<>();
        if (source == null) {
            return values;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            DecodeContext itemContext = context.child(entry.getKey());
            T mappedValue = mapper.apply(entry.getValue(), itemContext);
            if (mappedValue != null) {
                values.put(entry.getKey(), mappedValue);
            }
        }
        return values;
    }
}
