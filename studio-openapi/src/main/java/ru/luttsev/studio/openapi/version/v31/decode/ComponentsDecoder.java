package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ComponentsDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "schemas",
            "responses",
            "parameters",
            "examples",
            "requestBodies",
            "headers",
            "securitySchemes",
            "links",
            "callbacks",
            "pathItems");

    private final SchemaDecoder schemaDecoder = new SchemaDecoder();
    private final PayloadDecoder payloadDecoder =
            new PayloadDecoder(schemaDecoder);
    private final ReferenceOrDecoder referenceOrDecoder =
            new ReferenceOrDecoder();
    private final SecuritySchemeDecoder securitySchemeDecoder =
            new SecuritySchemeDecoder();
    private final PathItemDecoder pathItemDecoder =
            new PathItemDecoder(payloadDecoder);
    private final CallbackDecoder callbackDecoder = new CallbackDecoder();

    Components decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Components components = new Components();
        ObjectValue schemas = reader.optionalObject("schemas");
        if (schemas != null) {
            decodeSchemas(
                    schemas,
                    components,
                    context.child("schemas"));
        }
        components.setResponses(payloadDecoder.decodeResponses(
                reader.optionalObject("responses"),
                context.child("responses")));
        components.setParameters(payloadDecoder.decodeParameters(
                reader.optionalObject("parameters"),
                context.child("parameters")));
        components.setExamples(payloadDecoder.decodeExamples(
                reader.optionalObject("examples"),
                context.child("examples")));
        components.setRequestBodies(payloadDecoder.decodeRequestBodies(
                reader.optionalObject("requestBodies"),
                context.child("requestBodies")));
        components.setHeaders(payloadDecoder.decodeHeaders(
                reader.optionalObject("headers"),
                context.child("headers")));
        components.setSecuritySchemes(referenceOrDecoder.decodeMap(
                reader.optionalObject("securitySchemes"),
                context.child("securitySchemes"),
                securitySchemeDecoder::decode));
        components.setLinks(payloadDecoder.decodeLinks(
                reader.optionalObject("links"),
                context.child("links")));
        components.setCallbacks(callbackDecoder.decodeMap(
                reader.optionalObject("callbacks"),
                context.child("callbacks"),
                pathItemDecoder::decode));
        components.setPathItems(pathItemDecoder.decodeMap(
                reader.optionalObject("pathItems"),
                context.child("pathItems")));
        AdditionalFieldsMapper.copy(source, components, MAPPED_FIELDS);
        return components;
    }

    private void decodeSchemas(
            ObjectValue source,
            Components target,
            DecodeContext context) {
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            Schema schema = schemaDecoder.decode(
                    entry.getValue(),
                    context.child(entry.getKey()));
            if (schema != null) {
                target.getSchemas().put(entry.getKey(), schema);
            }
        }
    }
}
