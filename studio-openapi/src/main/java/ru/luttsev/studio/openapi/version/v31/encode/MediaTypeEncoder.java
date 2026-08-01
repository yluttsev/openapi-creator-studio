package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class MediaTypeEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "schema",
            "example",
            "examples",
            "encoding",
            "itemSchema",
            "prefixEncoding",
            "itemEncoding");

    ObjectValue encode(
            MediaType source,
            EncodeContext context,
            PayloadEncoder payloadEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getSchema() != null) {
            target.put(
                    "schema",
                    payloadEncoder.encodeSchema(
                            source.getSchema(),
                            context.child("schema")));
        }
        if (source.getExample() != null) {
            target.put("example", source.getExample());
        }
        if (source.getExamples() != null
                && !source.getExamples().isEmpty()) {
            target.put(
                    "examples",
                    payloadEncoder.encodeExamples(
                            source.getExamples(),
                            context.child("examples")));
        }
        if (source.getEncoding() != null
                && !source.getEncoding().isEmpty()) {
            target.put(
                    "encoding",
                    payloadEncoder.encodeEncodings(
                            source.getEncoding(),
                            context.child("encoding")));
        }

        if (source.getItemSchema() != null) {
            context.unsupportedField("itemSchema");
        }
        if (source.getPrefixEncoding() != null
                && !source.getPrefixEncoding().isEmpty()) {
            context.unsupportedField("prefixEncoding");
        }
        if (source.getItemEncoding() != null) {
            context.unsupportedField("itemEncoding");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
