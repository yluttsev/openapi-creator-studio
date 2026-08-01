package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.List;
import java.util.Set;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class ServerVariableDecoder {

    private static final Set<String> MAPPED_FIELDS =
            Set.of("enum", "default", "description");

    ServerVariable decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        ServerVariable variable = new ServerVariable();
        variable.setEnumValues(decodeEnum(
                reader.optionalArray("enum"),
                context.child("enum")));
        variable.setDefaultValue(reader.requiredString("default"));
        variable.setDescription(reader.optionalString("description"));
        AdditionalFieldsMapper.copy(source, variable, MAPPED_FIELDS);
        return variable;
    }

    private static List<String> decodeEnum(
            ArrayValue source,
            DecodeContext context) {
        return ArrayValueMapper.mapStrings(source, context);
    }
}
