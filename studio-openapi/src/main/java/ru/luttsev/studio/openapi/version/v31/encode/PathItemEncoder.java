package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class PathItemEncoder {

    private static final Set<String> OPENAPI_31_METHODS = Set.of(
            "GET",
            "PUT",
            "POST",
            "DELETE",
            "OPTIONS",
            "HEAD",
            "PATCH",
            "TRACE");
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
            "query",
            "additionalOperations",
            "servers",
            "parameters");

    private final PayloadEncoder payloadEncoder;
    private final OperationEncoder operationEncoder;
    private final ServerEncoder serverEncoder = new ServerEncoder();

    PathItemEncoder(PayloadEncoder payloadEncoder) {
        this.payloadEncoder = Objects.requireNonNull(
                payloadEncoder,
                "payloadEncoder must not be null");
        operationEncoder = new OperationEncoder(payloadEncoder);
    }

    ObjectValue encode(PathItem source, EncodeContext context) {
        if (!context.enter(source)) {
            context.mappingError(
                    OpenApiDiagnosticCodes.MAPPING_CYCLIC_INLINE_OBJECT,
                    "Cyclic inline Path Item cannot be encoded");
            return new ObjectValueBuilder().build();
        }
        try {
            return encodeValue(source, context);
        } finally {
            context.leave(source);
        }
    }

    private ObjectValue encodeValue(PathItem source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getRef() != null) {
            target.putString("$ref", source.getRef().value());
        }
        target.putString("summary", source.getSummary())
                .putString("description", source.getDescription());
        encodeOperations(source.getOperations(), target, context);
        if (source.getServers() != null && !source.getServers().isEmpty()) {
            target.put(
                    "servers",
                    serverEncoder.encodeList(
                            source.getServers(),
                            context.child("servers")));
        }
        if (source.getParameters() != null
                && !source.getParameters().isEmpty()) {
            target.put(
                    "parameters",
                    payloadEncoder.encodeParameterList(
                            source.getParameters(),
                            context.child("parameters")));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }

    ObjectValue encodeMap(
            Map<String, PathItem> source,
            EncodeContext context) {
        return ObjectValueEncoder.encodeObjects(
                source,
                context,
                this::encode);
    }

    private void encodeOperations(
            Map<HttpMethod, Operation> source,
            ObjectValueBuilder target,
            EncodeContext context) {
        if (source == null) {
            return;
        }
        for (Map.Entry<HttpMethod, Operation> entry : source.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            String method = entry.getKey().value();
            String canonicalMethod = method.toUpperCase(Locale.ROOT);
            if (OPENAPI_31_METHODS.contains(canonicalMethod)) {
                String field = canonicalMethod.toLowerCase(Locale.ROOT);
                target.put(
                        field,
                        operationEncoder.encode(
                                entry.getValue(),
                                context.child(field),
                                this));
            } else {
                reportUnsupportedMethod(method, canonicalMethod, context);
            }
        }
    }

    private static void reportUnsupportedMethod(
            String method,
            String canonicalMethod,
            EncodeContext context) {
        EncodeContext methodContext = "QUERY".equals(canonicalMethod)
                ? context.child("query")
                : context.child("additionalOperations").child(method);
        methodContext.error(
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                "HTTP method '" + method + "' is not supported by OpenAPI "
                        + context.targetVersion().value());
    }
}
