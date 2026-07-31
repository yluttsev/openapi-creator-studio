package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class PathItemDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "$ref",
            "summary",
            "description",
            "get",
            "put",
            "post",
            "delete",
            "options",
            "head",
            "patch",
            "trace",
            "servers",
            "parameters");

    private final PayloadDecoder payloadDecoder;
    private final OperationDecoder operationDecoder;
    private final ServerDecoder serverDecoder = new ServerDecoder();

    PathItemDecoder(PayloadDecoder payloadDecoder) {
        this.payloadDecoder = Objects.requireNonNull(
                payloadDecoder,
                "payloadDecoder must not be null");
        operationDecoder = new OperationDecoder(payloadDecoder);
    }

    PathItem decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        PathItem pathItem = new PathItem();
        pathItem.setRef(reader.optionalUriReference("$ref"));
        pathItem.setSummary(reader.optionalString("summary"));
        pathItem.setDescription(reader.optionalString("description"));
        decodeOperation(reader, "get", HttpMethod.GET, pathItem, context);
        decodeOperation(reader, "put", HttpMethod.PUT, pathItem, context);
        decodeOperation(reader, "post", HttpMethod.POST, pathItem, context);
        decodeOperation(reader, "delete", HttpMethod.DELETE, pathItem, context);
        decodeOperation(reader, "options", HttpMethod.OPTIONS, pathItem, context);
        decodeOperation(reader, "head", HttpMethod.HEAD, pathItem, context);
        decodeOperation(reader, "patch", HttpMethod.PATCH, pathItem, context);
        decodeOperation(reader, "trace", HttpMethod.TRACE, pathItem, context);
        pathItem.setServers(serverDecoder.decodeList(
                reader.optionalArray("servers"),
                context.child("servers")));
        pathItem.setParameters(payloadDecoder.decodeParameterList(
                reader.optionalArray("parameters"),
                context.child("parameters")));
        AdditionalFieldsMapper.copy(source, pathItem, MAPPED_FIELDS);
        return pathItem;
    }

    Map<String, PathItem> decodeMap(
            ObjectValue source,
            DecodeContext context) {
        return ObjectValueMapper.mapObjects(source, context, this::decode);
    }

    private void decodeOperation(
            ObjectValueReader reader,
            String field,
            HttpMethod method,
            PathItem target,
            DecodeContext context) {
        ObjectValue operationSource = reader.optionalObject(field);
        if (operationSource == null) {
            return;
        }
        Operation operation = operationDecoder.decode(
                operationSource,
                context.child(field),
                this);
        target.getOperations().put(method, operation);
    }
}
