package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ReferenceObjectEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("$ref", "summary", "description");

    ObjectValue encode(
            ReferenceObject<?> source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getRef() != null) {
            target.putString("$ref", source.getRef().value());
        }
        target.putString("summary", source.getSummary())
                .putString("description", source.getDescription());
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
