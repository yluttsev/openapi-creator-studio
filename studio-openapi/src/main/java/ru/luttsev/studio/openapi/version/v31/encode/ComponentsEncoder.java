package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ComponentsEncoder {

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
            "pathItems",
            "mediaTypes");

    private final SchemaEncoder schemaEncoder = new SchemaEncoder();
    private final PayloadEncoder payloadEncoder =
            new PayloadEncoder(schemaEncoder);
    private final ReferenceOrEncoder referenceOrEncoder =
            new ReferenceOrEncoder();
    private final SecuritySchemeEncoder securitySchemeEncoder =
            new SecuritySchemeEncoder();
    private final PathItemEncoder pathItemEncoder =
            new PathItemEncoder(payloadEncoder);
    private final CallbackEncoder callbackEncoder = new CallbackEncoder();

    ObjectValue encode(Components source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (notEmpty(source.getSchemas())) {
            target.put(
                    "schemas",
                    encodeSchemas(
                            source.getSchemas(),
                            context.child("schemas")));
        }
        if (notEmpty(source.getResponses())) {
            target.put(
                    "responses",
                    payloadEncoder.encodeResponses(
                            source.getResponses(),
                            context.child("responses")));
        }
        if (notEmpty(source.getParameters())) {
            target.put(
                    "parameters",
                    payloadEncoder.encodeParameters(
                            source.getParameters(),
                            context.child("parameters")));
        }
        if (notEmpty(source.getExamples())) {
            target.put(
                    "examples",
                    payloadEncoder.encodeExamples(
                            source.getExamples(),
                            context.child("examples")));
        }
        if (notEmpty(source.getRequestBodies())) {
            target.put(
                    "requestBodies",
                    payloadEncoder.encodeRequestBodies(
                            source.getRequestBodies(),
                            context.child("requestBodies")));
        }
        if (notEmpty(source.getHeaders())) {
            target.put(
                    "headers",
                    payloadEncoder.encodeHeaders(
                            source.getHeaders(),
                            context.child("headers")));
        }
        if (notEmpty(source.getSecuritySchemes())) {
            target.put(
                    "securitySchemes",
                    referenceOrEncoder.encodeMap(
                            source.getSecuritySchemes(),
                            context.child("securitySchemes"),
                            securitySchemeEncoder::encode));
        }
        if (notEmpty(source.getLinks())) {
            target.put(
                    "links",
                    payloadEncoder.encodeLinks(
                            source.getLinks(),
                            context.child("links")));
        }
        if (notEmpty(source.getCallbacks())) {
            target.put(
                    "callbacks",
                    callbackEncoder.encodeMap(
                            source.getCallbacks(),
                            context.child("callbacks"),
                            pathItemEncoder::encode));
        }
        if (notEmpty(source.getPathItems())) {
            target.put(
                    "pathItems",
                    pathItemEncoder.encodeMap(
                            source.getPathItems(),
                            context.child("pathItems")));
        }
        if (notEmpty(source.getMediaTypes())) {
            context.unsupportedField("mediaTypes");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }

    private ObjectValue encodeSchemas(
            Map<String, Schema> source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        for (Map.Entry<String, Schema> entry : source.entrySet()) {
            target.put(
                    entry.getKey(),
                    schemaEncoder.encode(
                            entry.getValue(),
                            context.child(entry.getKey())));
        }
        return target.build();
    }

    private static boolean notEmpty(Map<?, ?> value) {
        return value != null && !value.isEmpty();
    }
}
