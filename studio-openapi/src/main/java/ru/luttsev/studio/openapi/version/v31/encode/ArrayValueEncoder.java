package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.StringValue;

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
            values.add(encoder.apply(
                    source.get(index),
                    context.child(Integer.toString(index))));
        }
        return new ArrayValue(values);
    }
}
