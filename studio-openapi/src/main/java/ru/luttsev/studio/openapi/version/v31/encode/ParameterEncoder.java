package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ParameterEncoder {

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

    ObjectValue encode(
            Parameter source,
            EncodeContext context,
            PayloadEncoder payloadEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("name", source.getName())
                .putString(
                        "in",
                        ParameterValueEncoder.location(
                                source.getLocation(),
                                context.child("in")))
                .putString("description", source.getDescription())
                .putBoolean("required", source.getRequired())
                .putBoolean("deprecated", source.getDeprecated())
                .putBoolean("allowEmptyValue", source.getAllowEmptyValue())
                .putString("style", ParameterValueEncoder.style(source.getStyle()))
                .putBoolean("explode", source.getExplode())
                .putBoolean("allowReserved", source.getAllowReserved());

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
        if (source.getContent() != null
                && !source.getContent().isEmpty()) {
            target.put(
                    "content",
                    payloadEncoder.encodeContent(
                            source.getContent(),
                            context.child("content")));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
