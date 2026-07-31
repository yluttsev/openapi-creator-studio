package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ServerDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("url", "description", "variables");

    private final ServerVariableDecoder serverVariableDecoder =
            new ServerVariableDecoder();

    Server decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Server server = new Server();
        server.setUrl(reader.requiredString("url"));
        server.setDescription(reader.optionalString("description"));
        server.setVariables(decodeVariables(
                reader.optionalObject("variables"),
                context.child("variables")));
        AdditionalFieldsMapper.copy(source, server, MAPPED_FIELDS);
        return server;
    }

    List<Server> decodeList(
            ArrayValue source,
            DecodeContext context) {
        return ArrayValueMapper.mapObjects(source, context, this::decode);
    }

    private Map<String, ServerVariable> decodeVariables(
            ObjectValue source,
            DecodeContext context) {
        LinkedHashMap<String, ServerVariable> variables = new LinkedHashMap<>();
        if (source == null) {
            return variables;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            DecodeContext variableContext = context.child(entry.getKey());
            if (entry.getValue() instanceof ObjectValue objectValue) {
                variables.put(
                        entry.getKey(),
                        serverVariableDecoder.decode(objectValue, variableContext));
            } else {
                variableContext.error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected object but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
        return variables;
    }
}
