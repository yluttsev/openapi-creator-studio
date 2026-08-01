package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ArrayValueEncoder {

    private ArrayValueEncoder() {
    }

    static ArrayValue encodeStrings(List<String> source) {
        ArrayList<DocumentValue> values = new ArrayList<>();
        if (source != null) {
            for (String value : source) {
                values.add(new StringValue(value));
            }
        }
        return new ArrayValue(values);
    }

    static <T> ArrayValue encodeObjects(
            List<T> source,
            EncodeContext context,
            BiFunction<T, EncodeContext, ? extends DocumentValue> encoder) {
        ArrayList<DocumentValue> values = new ArrayList<>();
        if (source == null) {
            return new ArrayValue(values);
        }

        for (int index = 0; index < source.size(); index++) {
            EncodeContext itemContext = context.child(Integer.toString(index));
            T value = source.get(index);
            if (value == null) {
                itemContext.mappingError(
                        OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                        "Collection element must not be null");
                continue;
            }
            values.add(encoder.apply(value, itemContext));
        }
        return new ArrayValue(values);
    }
}
