package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class MediaTypeDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("schema", "example", "examples", "encoding");

    MediaType decode(
            ObjectValue source,
            DecodeContext context,
            PayloadDecoder payloadDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        MediaType mediaType = new MediaType();

        DocumentValue schema = reader.optionalValue("schema");
        if (schema != null) {
            mediaType.setSchema(payloadDecoder.decodeSchema(
                    schema,
                    context.child("schema")));
        }
        mediaType.setExample(reader.optionalValue("example"));
        mediaType.setExamples(payloadDecoder.decodeExamples(
                reader.optionalObject("examples"),
                context.child("examples")));
        mediaType.setEncoding(payloadDecoder.decodeEncodings(
                reader.optionalObject("encoding"),
                context.child("encoding")));

        AdditionalFieldsMapper.copy(source, mediaType, MAPPED_FIELDS);
        return mediaType;
    }
}
