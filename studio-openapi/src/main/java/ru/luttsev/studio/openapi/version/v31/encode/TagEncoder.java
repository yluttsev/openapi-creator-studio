package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.List;
import java.util.Set;
import ru.luttsev.studio.core.model.tag.Tag;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class TagEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "name",
            "summary",
            "description",
            "externalDocs",
            "parent",
            "kind");

    private final ExternalDocumentationEncoder externalDocumentationEncoder =
            new ExternalDocumentationEncoder();

    ArrayValue encodeList(List<Tag> source, EncodeContext context) {
        return ArrayValueEncoder.encodeObjects(
                source,
                context,
                this::encode);
    }

    private ObjectValue encode(Tag source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString("name", source.getName())
                .putString("description", source.getDescription());
        if (source.getExternalDocs() != null) {
            target.put(
                    "externalDocs",
                    externalDocumentationEncoder.encode(
                            source.getExternalDocs(),
                            context.child("externalDocs")));
        }
        if (source.getSummary() != null) {
            context.unsupportedField("summary");
        }
        if (source.getParent() != null) {
            context.unsupportedField("parent");
        }
        if (source.getKind() != null) {
            context.unsupportedField("kind");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
