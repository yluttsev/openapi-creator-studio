package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ApiResponseDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("description", "headers", "content", "links");

    ApiResponse decode(
            ObjectValue source,
            DecodeContext context,
            PayloadDecoder payloadDecoder) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        ApiResponse response = new ApiResponse();
        response.setDescription(reader.requiredString("description"));
        response.setHeaders(payloadDecoder.decodeHeaders(
                reader.optionalObject("headers"),
                context.child("headers")));
        response.setContent(payloadDecoder.decodeContent(
                reader.optionalObject("content"),
                context.child("content")));
        response.setLinks(payloadDecoder.decodeLinks(
                reader.optionalObject("links"),
                context.child("links")));
        AdditionalFieldsMapper.copy(source, response, MAPPED_FIELDS);
        return response;
    }
}
