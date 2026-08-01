package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class SecurityRequirementEncoder {

    ArrayValue encodeList(
            List<SecurityRequirement> source,
            EncodeContext context) {
        return ArrayValueEncoder.encodeObjects(
                source,
                context,
                this::encode);
    }

    private ObjectValue encode(
            SecurityRequirement source,
            EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        for (Map.Entry<String, List<String>> entry
                : source.getRequirements().entrySet()) {
            ArrayValue scopes = ArrayValueEncoder.encodeStrings(entry.getValue());
            target.put(entry.getKey(), scopes);
        }
        return target.build();
    }
}
