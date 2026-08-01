package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class HeaderEncoder {

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

    ObjectValue encode(
            Header source,
            EncodeContext context,
            PayloadEncoder payloadEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("description", source.getDescription())
                .putBoolean("required", source.getRequired())
                .putBoolean("deprecated", source.getDeprecated())
                .putString("style", ParameterValueEncoder.style(source.getStyle()))
                .putBoolean("explode", source.getExplode());
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
