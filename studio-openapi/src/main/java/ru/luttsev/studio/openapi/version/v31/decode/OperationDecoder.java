package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

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
            "deprecated",
            "security",
            "servers");

    private final PayloadDecoder payloadDecoder;
    private final ResponsesDecoder responsesDecoder;
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

    Operation decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Operation operation = new Operation();
        operation.setTags(decodeStringList(
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

    private static List<String> decodeStringList(
            ArrayValue source,
            DecodeContext context) {
        ArrayList<String> values = new ArrayList<>();
        if (source == null) {
            return values;
        }

        for (int index = 0; index < source.values().size(); index++) {
            DocumentValue value = source.values().get(index);
            if (value instanceof StringValue stringValue) {
                values.add(stringValue.value());
            } else {
                context.child(Integer.toString(index)).error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected string but found "
                                + ObjectValueReader.typeOf(value));
            }
        }
        return values;
    }
}
