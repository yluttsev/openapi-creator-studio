package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class RequestBodyEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("description", "content", "required");

    ObjectValue encode(
            RequestBody source,
            EncodeContext context,
            PayloadEncoder payloadEncoder) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("description", source.getDescription())
                .put(
                        "content",
                        payloadEncoder.encodeContent(
                                source.getContent(),
                                context.child("content")))
                .putBoolean("required", source.getRequired());
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
