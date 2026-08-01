package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class OAuthFlowsEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "implicit",
            "password",
            "clientCredentials",
            "authorizationCode",
            "deviceAuthorization");

    private final OAuthFlowEncoder oauthFlowEncoder = new OAuthFlowEncoder();

    ObjectValue encode(OAuthFlows source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder();
        encodeFlow("implicit", source.getImplicit(), target, context);
        encodeFlow("password", source.getPassword(), target, context);
        encodeFlow(
                "clientCredentials",
                source.getClientCredentials(),
                target,
                context);
        encodeFlow(
                "authorizationCode",
                source.getAuthorizationCode(),
                target,
                context);
        if (source.getDeviceAuthorization() != null) {
            context.unsupportedField("deviceAuthorization");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }

    private void encodeFlow(
            String field,
            OAuthFlow source,
            ObjectValueBuilder target,
            EncodeContext context) {
        if (source != null) {
            target.put(
                    field,
                    oauthFlowEncoder.encode(
                            source,
                            context.child(field)));
        }
    }
}
