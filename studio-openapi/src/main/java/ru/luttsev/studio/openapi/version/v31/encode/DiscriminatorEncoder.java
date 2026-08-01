package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class DiscriminatorEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("propertyName", "mapping", "defaultMapping");

    ObjectValue encode(
            Discriminator source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("propertyName", source.getPropertyName());

        if (source.getMapping() != null
                && !source.getMapping().isEmpty()) {
            target.put(
                    "mapping",
                    encodeMapping(source.getMapping()));
        }

        if (source.getDefaultMapping() != null) {
            context.unsupportedField("defaultMapping");
        }

        if (source.getExtensions() != null) {
            AdditionalFieldsEncoder.copy(
                    source.getExtensions(),
                    "Discriminator extension",
                    target,
                    MAPPED_FIELDS,
                    context);
        }
        return target.build();
    }

    private static ObjectValue encodeMapping(
            Map<String, String> source) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            target.putString(entry.getKey(), entry.getValue());
        }
        return target.build();
    }
}
