package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class LinkDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "operationRef",
            "operationId",
            "parameters",
            "requestBody",
            "description",
            "server");

    private final ServerDecoder serverDecoder = new ServerDecoder();

    Link decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Link link = new Link();
        link.setOperationRef(reader.optionalUriReference("operationRef"));
        link.setOperationId(reader.optionalString("operationId"));
        link.setParameters(decodeParameters(
                reader.optionalObject("parameters"),
                context.child("parameters")));
        link.setRequestBody(reader.optionalValue("requestBody"));
        link.setDescription(reader.optionalString("description"));

        ObjectValue serverSource = reader.optionalObject("server");
        if (serverSource != null) {
            Server server = serverDecoder.decode(
                    serverSource,
                    context.child("server"));
            link.setServer(server);
        }

        validateOperationTarget(source, context);
        AdditionalFieldsMapper.copy(source, link, MAPPED_FIELDS);
        return link;
    }

    private static Map<String, DocumentValue> decodeParameters(
            ObjectValue source,
            DecodeContext context) {
        LinkedHashMap<String, DocumentValue> parameters = new LinkedHashMap<>();
        if (source == null) {
            return parameters;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (entry.getValue() instanceof StringValue) {
                parameters.put(entry.getKey(), entry.getValue());
            } else {
                context.child(entry.getKey()).error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected string but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
        return parameters;
    }

    private static void validateOperationTarget(
            ObjectValue source,
            DecodeContext context) {
        boolean hasOperationRef = source.values().containsKey("operationRef");
        boolean hasOperationId = source.values().containsKey("operationId");
        if (!hasOperationRef && !hasOperationId) {
            context.error(
                    OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                    "Link requires either 'operationRef' or 'operationId'");
        } else if (hasOperationRef && hasOperationId) {
            context.error(
                    OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                    "Link cannot contain both 'operationRef' and 'operationId'");
        }
    }
}
