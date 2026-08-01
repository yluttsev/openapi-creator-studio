package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class EncodingEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "contentType",
            "headers",
            "style",
            "explode",
            "allowReserved",
            "encoding",
            "prefixEncoding",
            "itemEncoding");

    ObjectValue encode(
            Encoding source,
            EncodeContext context,
            PayloadEncoder payloadEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("contentType", source.getContentType())
                .putString("style", ParameterValueEncoder.style(source.getStyle()))
                .putBoolean("explode", source.getExplode())
                .putBoolean("allowReserved", source.getAllowReserved());
        if (source.getHeaders() != null
                && !source.getHeaders().isEmpty()) {
            target.put(
                    "headers",
                    payloadEncoder.encodeHeaders(
                            source.getHeaders(),
                            context.child("headers")));
        }

        if (source.getEncoding() != null
                && !source.getEncoding().isEmpty()) {
            context.unsupportedField("encoding");
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
