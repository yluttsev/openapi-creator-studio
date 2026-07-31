package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class AdditionalFieldsEncoder {

    private AdditionalFieldsEncoder() {
    }

    static void copy(
            ExtensibleObject source,
            ObjectValueBuilder target,
            Set<String> mappedFields,
            EncodeContext context) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(mappedFields, "mappedFields must not be null");
        Objects.requireNonNull(context, "context must not be null");

        for (Map.Entry<String, DocumentValue> entry
                : source.getAdditionalFields().entrySet()) {
            String field = entry.getKey();
            if (mappedFields.contains(field)) {
                context.child(field).error(
                        OpenApiDiagnosticCodes.VERSION_FIELD_CONFLICT,
                        "Additional field '" + field
                                + "' conflicts with a mapped OpenAPI "
                                + context.targetVersion().value()
                                + " field");
                continue;
            }
            target.put(field, entry.getValue());
        }
    }
}
