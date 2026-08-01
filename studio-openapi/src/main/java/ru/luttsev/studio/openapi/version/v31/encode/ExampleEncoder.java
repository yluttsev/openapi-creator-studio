package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ExampleEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "summary",
            "description",
            "value",
            "externalValue",
            "dataValue",
            "serializedValue");

    ObjectValue encode(Example source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("summary", source.getSummary())
                .putString("description", source.getDescription());
        if (source.getValue() != null) {
            target.put("value", source.getValue());
        }
        if (source.getExternalValue() != null) {
            target.putString(
                    "externalValue",
                    source.getExternalValue().value());
        }
        if (source.getDataValue() != null) {
            context.unsupportedField("dataValue");
        }
        if (source.getSerializedValue() != null) {
            context.unsupportedField("serializedValue");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
