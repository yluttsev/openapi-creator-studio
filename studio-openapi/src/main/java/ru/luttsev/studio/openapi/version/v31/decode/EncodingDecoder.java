package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class EncodingDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("contentType", "headers", "style", "explode", "allowReserved");

    Encoding decode(
            ObjectValue source,
            DecodeContext context,
            PayloadDecoder payloadDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Encoding encoding = new Encoding();
        encoding.setContentType(reader.optionalString("contentType"));
        encoding.setHeaders(payloadDecoder.decodeHeaders(
                reader.optionalObject("headers"),
                context.child("headers")));
        encoding.setStyle(ParameterValueDecoder.style(
                reader.optionalString("style"),
                context.child("style")));
        encoding.setExplode(reader.optionalBoolean("explode"));
        encoding.setAllowReserved(reader.optionalBoolean("allowReserved"));
        AdditionalFieldsMapper.copy(source, encoding, MAPPED_FIELDS);
        return encoding;
    }
}
