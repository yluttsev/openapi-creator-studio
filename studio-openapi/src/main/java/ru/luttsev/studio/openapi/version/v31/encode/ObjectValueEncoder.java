package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ObjectValueEncoder {

    private ObjectValueEncoder() {
    }

    static <T> ObjectValue encodeObjects(
            Map<String, T> source,
            EncodeContext context,
            BiFunction<T, EncodeContext, ? extends DocumentValue> encoder) {
        LinkedHashMap<String, DocumentValue> values = new LinkedHashMap<>();
        if (source == null) {
            return new ObjectValue(values);
        }

        for (Map.Entry<String, T> entry : source.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                context.mappingError(
                        OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                        "Map key must not be null");
                continue;
            }

            EncodeContext itemContext = context.child(key);
            T value = entry.getValue();
            if (value == null) {
                itemContext.mappingError(
                        OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                        "Map value must not be null");
                continue;
            }
            values.put(key, encoder.apply(value, itemContext));
        }
        return new ObjectValue(values);
    }
}
