package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ExampleDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("summary", "description", "value", "externalValue");

    Example decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        Example example = new Example();
        example.setSummary(reader.optionalString("summary"));
        example.setDescription(reader.optionalString("description"));
        example.setValue(reader.optionalValue("value"));
        example.setExternalValue(reader.optionalUriReference("externalValue"));
        AdditionalFieldsMapper.copy(source, example, MAPPED_FIELDS);
        return example;
    }
}
