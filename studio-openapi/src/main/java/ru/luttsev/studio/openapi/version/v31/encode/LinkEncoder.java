package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashMap;
import java.util.Set;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class LinkEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "operationRef",
            "operationId",
            "parameters",
            "requestBody",
            "description",
            "server");

    private final ServerEncoder serverEncoder = new ServerEncoder();

    ObjectValue encode(Link source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getOperationRef() != null) {
            target.putString(
                    "operationRef",
                    source.getOperationRef().value());
        }
        target.putString("operationId", source.getOperationId());
        if (source.getParameters() != null
                && !source.getParameters().isEmpty()) {
            target.put(
                    "parameters",
                    new ObjectValue(new LinkedHashMap<>(
                            source.getParameters())));
        }
        if (source.getRequestBody() != null) {
            target.put("requestBody", source.getRequestBody());
        }
        target.putString("description", source.getDescription());
        if (source.getServer() != null) {
            target.put(
                    "server",
                    serverEncoder.encode(
                            source.getServer(),
                            context.child("server")));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
