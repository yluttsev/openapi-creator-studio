package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class OperationDecoder {

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

    private final PayloadDecoder payloadDecoder;
    private final ResponsesDecoder responsesDecoder;
    private final CallbackDecoder callbackDecoder = new CallbackDecoder();
    private final ServerDecoder serverDecoder = new ServerDecoder();
    private final SecurityRequirementDecoder securityRequirementDecoder =
            new SecurityRequirementDecoder();
    private final ExternalDocumentationDecoder externalDocumentationDecoder =
            new ExternalDocumentationDecoder();

    OperationDecoder(PayloadDecoder payloadDecoder) {
        this.payloadDecoder = Objects.requireNonNull(
                payloadDecoder,
                "payloadDecoder must not be null");
        responsesDecoder = new ResponsesDecoder(payloadDecoder);
    }

    Operation decode(
            ObjectValue source,
            DecodeContext context,
            PathItemDecoder pathItemDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Operation operation = new Operation();
        operation.setTags(ArrayValueMapper.mapStrings(
                reader.optionalArray("tags"),
                context.child("tags")));
        operation.setSummary(reader.optionalString("summary"));
        operation.setDescription(reader.optionalString("description"));
        operation.setOperationId(reader.optionalString("operationId"));
        operation.setParameters(payloadDecoder.decodeParameterList(
                reader.optionalArray("parameters"),
                context.child("parameters")));

        ObjectValue externalDocsSource = reader.optionalObject("externalDocs");
        if (externalDocsSource != null) {
            ExternalDocumentation externalDocs =
                    externalDocumentationDecoder.decode(
                            externalDocsSource,
                            context.child("externalDocs"));
            operation.setExternalDocs(externalDocs);
        }

        DocumentValue requestBodySource = reader.optionalValue("requestBody");
        if (requestBodySource != null) {
            ReferenceOr<RequestBody> requestBody = payloadDecoder.decodeRequestBody(
                    requestBodySource,
                    context.child("requestBody"));
            operation.setRequestBody(requestBody);
        }

        ObjectValue responsesSource = reader.requiredObject("responses");
        if (responsesSource != null) {
            operation.setResponses(responsesDecoder.decode(
                    responsesSource,
                    context.child("responses")));
        }

        operation.setCallbacks(callbackDecoder.decodeMap(
                reader.optionalObject("callbacks"),
                context.child("callbacks"),
                pathItemDecoder::decode));

        operation.setDeprecated(reader.optionalBoolean("deprecated"));
        operation.setSecurity(securityRequirementDecoder.decodeList(
                reader.optionalArray("security"),
                context.child("security")));
        operation.setServers(serverDecoder.decodeList(
                reader.optionalArray("servers"),
                context.child("servers")));
        AdditionalFieldsMapper.copy(source, operation, MAPPED_FIELDS);
        return operation;
    }
}
