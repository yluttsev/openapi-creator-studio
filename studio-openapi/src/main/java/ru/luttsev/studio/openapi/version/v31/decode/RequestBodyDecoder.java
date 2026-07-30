package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class RequestBodyDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("description", "content", "required");

    RequestBody decode(
            ObjectValue source,
            DecodeContext context,
            PayloadDecoder payloadDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription(reader.optionalString("description"));
        requestBody.setContent(payloadDecoder.decodeContent(
                reader.requiredObject("content"),
                context.child("content")));
        requestBody.setRequired(reader.optionalBoolean("required"));
        AdditionalFieldsMapper.copy(source, requestBody, MAPPED_FIELDS);
        return requestBody;
    }
}
