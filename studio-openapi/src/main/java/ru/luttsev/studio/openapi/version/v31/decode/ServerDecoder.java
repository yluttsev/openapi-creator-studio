package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

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
        return ObjectValueMapper.mapObjects(
                source,
                context,
                serverVariableDecoder::decode);
    }
}
