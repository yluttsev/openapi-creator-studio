package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ApiResponseEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("description", "headers", "content", "links");

    ObjectValue encode(
            ApiResponse source,
            EncodeContext context,
            PayloadEncoder payloadEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("description", source.getDescription());
        if (source.getHeaders() != null
                && !source.getHeaders().isEmpty()) {
            target.put(
                    "headers",
                    payloadEncoder.encodeHeaders(
                            source.getHeaders(),
                            context.child("headers")));
        }
        if (source.getContent() != null
                && !source.getContent().isEmpty()) {
            target.put(
                    "content",
                    payloadEncoder.encodeContent(
                            source.getContent(),
                            context.child("content")));
        }
        if (source.getLinks() != null
                && !source.getLinks().isEmpty()) {
            target.put(
                    "links",
                    payloadEncoder.encodeLinks(
                            source.getLinks(),
                            context.child("links")));
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
