package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class DiscriminatorDecoder {

    Discriminator decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName(reader.requiredString("propertyName"));
        decodeMapping(
                reader.optionalObject("mapping"),
                discriminator,
                context.child("mapping"));
        copyExtensions(source, discriminator);
        return discriminator;
    }

    private static void decodeMapping(
            ObjectValue source,
            Discriminator target,
            DecodeContext context) {
        if (source == null) {
            return;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (entry.getValue() instanceof StringValue stringValue) {
                target.getMapping().put(entry.getKey(), stringValue.value());
            } else {
                context.child(entry.getKey()).error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected string but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
    }

    private static void copyExtensions(
            ObjectValue source,
            Discriminator target) {
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (entry.getKey().startsWith("x-")) {
                target.getExtensions().put(entry.getKey(), entry.getValue());
            }
        }
    }
}
