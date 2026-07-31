package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.List;
import java.util.Set;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.tag.Tag;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class TagDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("name", "description", "externalDocs");

    private final ExternalDocumentationDecoder externalDocumentationDecoder =
            new ExternalDocumentationDecoder();

    List<Tag> decodeList(
            ArrayValue source,
            DecodeContext context) {
        return ArrayValueMapper.mapObjects(source, context, this::decode);
    }

    private Tag decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Tag tag = new Tag();
        tag.setName(reader.requiredString("name"));
        tag.setDescription(reader.optionalString("description"));

        ObjectValue externalDocsSource = reader.optionalObject("externalDocs");
        if (externalDocsSource != null) {
            ExternalDocumentation externalDocs =
                    externalDocumentationDecoder.decode(
                            externalDocsSource,
                            context.child("externalDocs"));
            tag.setExternalDocs(externalDocs);
        }

        AdditionalFieldsMapper.copy(source, tag, MAPPED_FIELDS);
        return tag;
    }
}
