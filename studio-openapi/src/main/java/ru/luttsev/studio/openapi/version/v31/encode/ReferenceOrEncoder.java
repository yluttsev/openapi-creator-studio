package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Map;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
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
        return ObjectValueEncoder.encodeObjects(
                source,
                context,
                (value, itemContext) -> encode(
                        value,
                        itemContext,
                        inlineEncoder));
    }

    @SuppressWarnings("unchecked")
    private static <T> InlineObject<T> asInlineObject(
            ReferenceOr<T> source) {
        return (InlineObject<T>) source;
    }
}
