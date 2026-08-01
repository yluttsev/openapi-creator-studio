package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ServerEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("url", "description", "variables", "name");

    private final ServerVariableEncoder serverVariableEncoder =
            new ServerVariableEncoder();

    ObjectValue encode(Server source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("url", source.getUrl())
                .putString("description", source.getDescription());
        if (source.getVariables() != null
                && !source.getVariables().isEmpty()) {
            target.put(
                    "variables",
                    encodeVariables(
                            source.getVariables(),
                            context.child("variables")));
        }
        if (source.getName() != null) {
            context.unsupportedField("name");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }

    private ObjectValue encodeVariables(
            Map<String, ServerVariable> source,
            EncodeContext context) {
        LinkedHashMap<String, DocumentValue> values = new LinkedHashMap<>();
        for (Map.Entry<String, ServerVariable> entry : source.entrySet()) {
            values.put(
                    entry.getKey(),
                    serverVariableEncoder.encode(
                            entry.getValue(),
                            context.child(entry.getKey())));
        }
        return new ObjectValue(values);
    }
}
