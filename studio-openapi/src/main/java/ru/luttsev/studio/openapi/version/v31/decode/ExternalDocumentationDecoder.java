package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ExternalDocumentationDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("description", "url");

    ExternalDocumentation decode(
            ObjectValue source,
            DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        ExternalDocumentation documentation = new ExternalDocumentation();
        documentation.setDescription(reader.optionalString("description"));
        documentation.setUrl(reader.requiredUriReference("url"));
        AdditionalFieldsMapper.copy(source, documentation, MAPPED_FIELDS);
        return documentation;
    }
}
