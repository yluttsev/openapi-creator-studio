package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.response.Responses;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class OperationEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "tags",
            "summary",
            "description",
            "externalDocs",
            "operationId",
            "parameters",
            "requestBody",
            "responses",
            "callbacks",
            "deprecated",
            "security",
            "servers");

    private final PayloadEncoder payloadEncoder;
    private final ResponsesEncoder responsesEncoder;
    private final CallbackEncoder callbackEncoder = new CallbackEncoder();
    private final ServerEncoder serverEncoder = new ServerEncoder();
    private final SecurityRequirementEncoder securityRequirementEncoder =
            new SecurityRequirementEncoder();
    private final ExternalDocumentationEncoder externalDocumentationEncoder =
            new ExternalDocumentationEncoder();

    OperationEncoder(PayloadEncoder payloadEncoder) {
        this.payloadEncoder = Objects.requireNonNull(
                payloadEncoder,
                "payloadEncoder must not be null");
        responsesEncoder = new ResponsesEncoder(payloadEncoder);
    }

    ObjectValue encode(
            Operation source,
            EncodeContext context,
            PathItemEncoder pathItemEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getTags() != null && !source.getTags().isEmpty()) {
            target.put("tags", ArrayValueEncoder.encodeStrings(source.getTags()));
        }
        target.putString("summary", source.getSummary())
                .putString("description", source.getDescription());
        if (source.getExternalDocs() != null) {
            target.put(
                    "externalDocs",
                    externalDocumentationEncoder.encode(
                            source.getExternalDocs(),
                            context.child("externalDocs")));
        }
        target.putString("operationId", source.getOperationId());
        if (source.getParameters() != null
                && !source.getParameters().isEmpty()) {
            target.put(
                    "parameters",
                    payloadEncoder.encodeParameterList(
                            source.getParameters(),
                            context.child("parameters")));
        }
        if (source.getRequestBody() != null) {
            target.put(
                    "requestBody",
                    payloadEncoder.encodeRequestBody(
                            source.getRequestBody(),
                            context.child("requestBody")));
        }
        target.put(
                "responses",
                encodeResponses(source.getResponses(), context.child("responses")));
        if (source.getCallbacks() != null
                && !source.getCallbacks().isEmpty()) {
            target.put(
                    "callbacks",
                    callbackEncoder.encodeMap(
                            source.getCallbacks(),
                            context.child("callbacks"),
                            pathItemEncoder::encode));
        }
        target.putBoolean("deprecated", source.getDeprecated());
        if (source.getSecurity() != null
                && !source.getSecurity().isEmpty()) {
            target.put(
                    "security",
                    securityRequirementEncoder.encodeList(
                            source.getSecurity(),
                            context.child("security")));
        }
        if (source.getServers() != null
                && !source.getServers().isEmpty()) {
            ArrayValue servers = serverEncoder.encodeList(
                    source.getServers(),
                    context.child("servers"));
            target.put("servers", servers);
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }

    private ObjectValue encodeResponses(
            Responses source,
            EncodeContext context) {
        return source == null
                ? new ObjectValueBuilder().build()
                : responsesEncoder.encode(source, context);
    }
}
