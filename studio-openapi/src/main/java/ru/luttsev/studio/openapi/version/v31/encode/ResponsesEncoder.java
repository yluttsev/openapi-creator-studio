package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.response.Responses;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ResponsesEncoder {

    private final PayloadEncoder payloadEncoder;

    ResponsesEncoder(PayloadEncoder payloadEncoder) {
        this.payloadEncoder = Objects.requireNonNull(
                payloadEncoder,
                "payloadEncoder must not be null");
    }

    ObjectValue encode(Responses source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        Set<String> mappedFields = new LinkedHashSet<>();
        for (Map.Entry<ResponseKey, ReferenceOr<ApiResponse>> entry
                : source.getValues().entrySet()) {
            String field = entry.getKey().value();
            mappedFields.add(field);
            target.put(
                    field,
                    payloadEncoder.encodeResponse(
                            entry.getValue(),
                            context.child(field)));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                mappedFields,
                context);
        return target.build();
    }
}
