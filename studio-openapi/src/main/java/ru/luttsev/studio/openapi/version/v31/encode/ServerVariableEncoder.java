package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.ArrayList;
import java.util.Set;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;

final class ServerVariableEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("enum", "default", "description");

    ObjectValue encode(
            ServerVariable source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getEnumValues() != null
                && !source.getEnumValues().isEmpty()) {
            ArrayList<DocumentValue> values =
                    new ArrayList<>(source.getEnumValues().size());
            for (String value : source.getEnumValues()) {
                values.add(new StringValue(value));
            }
            target.putArray("enum", values);
        }
        target.putString("default", source.getDefaultValue())
                .putString("description", source.getDescription());
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
