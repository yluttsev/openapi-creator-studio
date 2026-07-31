package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ArrayValueMapper {

    private ArrayValueMapper() {
    }

    static <T> List<T> mapObjects(
            ArrayValue source,
            DecodeContext context,
            BiFunction<ObjectValue, DecodeContext, T> mapper) {
        return mapValues(source, context, (value, itemContext) -> {
            if (value instanceof ObjectValue objectValue) {
                return mapper.apply(objectValue, itemContext);
            }
            reportTypeMismatch(value, "object", itemContext);
            return null;
        });
    }

    static List<String> mapStrings(
            ArrayValue source,
            DecodeContext context) {
        return mapValues(source, context, (value, itemContext) -> {
            if (value instanceof StringValue stringValue) {
                return stringValue.value();
            }
            reportTypeMismatch(value, "string", itemContext);
            return null;
        });
    }

    private static <T> List<T> mapValues(
            ArrayValue source,
            DecodeContext context,
            BiFunction<DocumentValue, DecodeContext, T> mapper) {
        ArrayList<T> values = new ArrayList<>();
        if (source == null) {
            return values;
        }

        for (int index = 0; index < source.values().size(); index++) {
            DocumentValue value = source.values().get(index);
            DecodeContext itemContext = context.child(Integer.toString(index));
            T mappedValue = mapper.apply(value, itemContext);
            if (mappedValue != null) {
                values.add(mappedValue);
            }
        }
        return values;
    }

    private static void reportTypeMismatch(
            DocumentValue value,
            String expectedType,
            DecodeContext context) {
        context.error(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "Expected " + expectedType + " but found "
                        + ObjectValueReader.typeOf(value));
    }
}
