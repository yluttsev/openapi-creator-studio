package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class CallbackEncoder {

    private final ReferenceOrEncoder referenceOrEncoder =
            new ReferenceOrEncoder();

    ObjectValue encodeMap(
            Map<String, ReferenceOr<Callback>> source,
            EncodeContext context,
            BiFunction<PathItem, EncodeContext, ObjectValue> pathItemEncoder) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                (callback, itemContext) ->
                        encode(callback, itemContext, pathItemEncoder));
    }

    private static ObjectValue encode(
            Callback source,
            EncodeContext context,
            BiFunction<PathItem, EncodeContext, ObjectValue> pathItemEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        Set<String> mappedFields = new LinkedHashSet<>();
        for (Map.Entry<String, PathItem> entry
                : source.getExpressions().entrySet()) {
            mappedFields.add(entry.getKey());
            target.put(
                    entry.getKey(),
                    pathItemEncoder.apply(
                            entry.getValue(),
                            context.child(entry.getKey())));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                mappedFields,
                context);
        return target.build();
    }
}
