package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ExternalDocumentationEncoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("description", "url");

    ObjectValue encode(
            ExternalDocumentation source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("description", source.getDescription());
        if (source.getUrl() != null) {
            target.putString("url", source.getUrl().value());
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
