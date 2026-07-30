package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class HeaderDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "description",
            "required",
            "deprecated",
            "style",
            "explode",
            "schema",
            "example",
            "examples",
            "content");

    Header decode(
            ObjectValue source,
            DecodeContext context,
            PayloadDecoder payloadDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Header header = new Header();
        header.setDescription(reader.optionalString("description"));
        header.setRequired(reader.optionalBoolean("required"));
        header.setDeprecated(reader.optionalBoolean("deprecated"));
        header.setStyle(ParameterValueDecoder.style(
                reader.optionalString("style"),
                context.child("style")));
        header.setExplode(reader.optionalBoolean("explode"));

        DocumentValue schema = reader.optionalValue("schema");
        if (schema != null) {
            header.setSchema(payloadDecoder.decodeSchema(
                    schema,
                    context.child("schema")));
        }
        header.setExample(reader.optionalValue("example"));
        header.setExamples(payloadDecoder.decodeExamples(
                reader.optionalObject("examples"),
                context.child("examples")));
        header.setContent(payloadDecoder.decodeContent(
                reader.optionalObject("content"),
                context.child("content")));

        AdditionalFieldsMapper.copy(source, header, MAPPED_FIELDS);
        return header;
    }
}
