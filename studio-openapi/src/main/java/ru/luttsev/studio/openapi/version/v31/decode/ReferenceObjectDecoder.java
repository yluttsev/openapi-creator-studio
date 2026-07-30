package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ReferenceObjectDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("$ref", "summary", "description");

    <T> ReferenceObject<T> decode(
            ObjectValue source,
            DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        ReferenceObject<T> reference = new ReferenceObject<>();
        reference.setRef(reader.requiredUriReference("$ref"));
        reference.setSummary(reader.optionalString("summary"));
        reference.setDescription(reader.optionalString("description"));
        AdditionalFieldsMapper.copy(source, reference, MAPPED_FIELDS);
        return reference;
    }
}
