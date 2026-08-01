package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ReferenceOrEncoder {

    private final ReferenceObjectEncoder referenceObjectEncoder =
            new ReferenceObjectEncoder();

    <T> ObjectValue encode(
            ReferenceOr<T> source,
            EncodeContext context,
            BiFunction<T, EncodeContext, ObjectValue> inlineEncoder) {
        if (source instanceof ReferenceObject<?> referenceObject) {
            return referenceObjectEncoder.encode(referenceObject, context);
        }

        InlineObject<T> inlineObject = asInlineObject(source);
        return inlineEncoder.apply(inlineObject.value(), context);
    }

    <T> ObjectValue encodeMap(
            Map<String, ReferenceOr<T>> source,
            EncodeContext context,
            BiFunction<T, EncodeContext, ObjectValue> inlineEncoder) {
        LinkedHashMap<String, DocumentValue> values = new LinkedHashMap<>();
        if (source == null) {
            return new ObjectValue(values);
        }

        for (Map.Entry<String, ReferenceOr<T>> entry : source.entrySet()) {
            values.put(
                    entry.getKey(),
                    encode(
                            entry.getValue(),
                            context.child(entry.getKey()),
                            inlineEncoder));
        }
        return new ObjectValue(values);
    }

    @SuppressWarnings("unchecked")
    private static <T> InlineObject<T> asInlineObject(
            ReferenceOr<T> source) {
        return (InlineObject<T>) source;
    }
}
