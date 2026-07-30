package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

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
        ArrayList<String> values = new ArrayList<>();
        if (source == null) {
            return values;
        }

        for (int index = 0; index < source.values().size(); index++) {
            DocumentValue value = source.values().get(index);
            if (value instanceof StringValue stringValue) {
                values.add(stringValue.value());
            } else {
                context.child(Integer.toString(index)).error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected string but found "
                                + ObjectValueReader.typeOf(value));
            }
        }
        return values;
    }
}
