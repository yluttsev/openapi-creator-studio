package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class AdditionalFieldsMapper {

    private AdditionalFieldsMapper() {
    }

    static void copy(
            ObjectValue source,
            ExtensibleObject target,
            Set<String> mappedFields) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(mappedFields, "mappedFields must not be null");

        for (Map.Entry<String, DocumentValue> field :
                source.values().entrySet()) {
            if (!mappedFields.contains(field.getKey())) {
                target.getAdditionalFields().put(
                        field.getKey(),
                        field.getValue());
            }
        }
    }
}
