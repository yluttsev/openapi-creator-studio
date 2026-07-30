package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ParameterDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "name",
            "in",
            "description",
            "required",
            "deprecated",
            "allowEmptyValue",
            "style",
            "explode",
            "allowReserved",
            "schema",
            "example",
            "examples",
            "content");

    Parameter decode(
            ObjectValue source,
            DecodeContext context,
            PayloadDecoder payloadDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Parameter parameter = new Parameter();
        parameter.setName(reader.requiredString("name"));
        parameter.setLocation(ParameterValueDecoder.location(
                reader.requiredString("in"),
                context.child("in")));
        parameter.setDescription(reader.optionalString("description"));
        parameter.setRequired(reader.optionalBoolean("required"));
        parameter.setDeprecated(reader.optionalBoolean("deprecated"));
        parameter.setAllowEmptyValue(reader.optionalBoolean("allowEmptyValue"));
        parameter.setStyle(ParameterValueDecoder.style(
                reader.optionalString("style"),
                context.child("style")));
        parameter.setExplode(reader.optionalBoolean("explode"));
        parameter.setAllowReserved(reader.optionalBoolean("allowReserved"));

        DocumentValue schema = reader.optionalValue("schema");
        if (schema != null) {
            parameter.setSchema(payloadDecoder.decodeSchema(
                    schema,
                    context.child("schema")));
        }
        parameter.setExample(reader.optionalValue("example"));
        parameter.setExamples(payloadDecoder.decodeExamples(
                reader.optionalObject("examples"),
                context.child("examples")));
        parameter.setContent(payloadDecoder.decodeContent(
                reader.optionalObject("content"),
                context.child("content")));

        AdditionalFieldsMapper.copy(source, parameter, MAPPED_FIELDS);
        return parameter;
    }
}
