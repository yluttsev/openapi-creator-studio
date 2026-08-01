package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class OAuthFlowEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "authorizationUrl",
            "deviceAuthorizationUrl",
            "tokenUrl",
            "refreshUrl",
            "scopes");

    ObjectValue encode(OAuthFlow source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source.getAuthorizationUrl() != null) {
            target.putString(
                    "authorizationUrl",
                    source.getAuthorizationUrl().value());
        }
        if (source.getTokenUrl() != null) {
            target.putString("tokenUrl", source.getTokenUrl().value());
        }
        if (source.getRefreshUrl() != null) {
            target.putString("refreshUrl", source.getRefreshUrl().value());
        }
        target.put("scopes", encodeScopes(source.getScopes()));
        if (source.getDeviceAuthorizationUrl() != null) {
            context.unsupportedField("deviceAuthorizationUrl");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }

    private static ObjectValue encodeScopes(Map<String, String> source) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        if (source == null) {
            return target.build();
        }
        for (Map.Entry<String, String> entry : source.entrySet()) {
            target.putString(entry.getKey(), entry.getValue());
        }
        return target.build();
    }
}
