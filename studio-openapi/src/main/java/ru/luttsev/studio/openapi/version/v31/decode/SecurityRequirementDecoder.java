package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class SecurityRequirementDecoder {

    List<SecurityRequirement> decodeList(
            ArrayValue source,
            DecodeContext context) {
        return ArrayValueMapper.mapObjects(
                source,
                context,
                SecurityRequirementDecoder::decode);
    }

    private static SecurityRequirement decode(
            ObjectValue source,
            DecodeContext context) {
        SecurityRequirement requirement = new SecurityRequirement();
        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            DecodeContext scopesContext = context.child(entry.getKey());
            if (entry.getValue() instanceof ArrayValue arrayValue) {
                requirement.require(
                        entry.getKey(),
                        decodeScopes(arrayValue, scopesContext));
            } else {
                scopesContext.error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected array but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
        return requirement;
    }

    private static List<String> decodeScopes(
            ArrayValue source,
            DecodeContext context) {
        return ArrayValueMapper.mapStrings(source, context);
    }
}
