package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class OAuthFlowsDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "implicit",
            "password",
            "clientCredentials",
            "authorizationCode");

    private final OAuthFlowDecoder oauthFlowDecoder = new OAuthFlowDecoder();

    OAuthFlows decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        OAuthFlows flows = new OAuthFlows();

        ObjectValue implicit = reader.optionalObject("implicit");
        if (implicit != null) {
            flows.setImplicit(oauthFlowDecoder.decodeImplicit(
                    implicit,
                    context.child("implicit")));
        }

        ObjectValue password = reader.optionalObject("password");
        if (password != null) {
            flows.setPassword(oauthFlowDecoder.decodePassword(
                    password,
                    context.child("password")));
        }

        ObjectValue clientCredentials = reader.optionalObject("clientCredentials");
        if (clientCredentials != null) {
            flows.setClientCredentials(oauthFlowDecoder.decodeClientCredentials(
                    clientCredentials,
                    context.child("clientCredentials")));
        }

        ObjectValue authorizationCode = reader.optionalObject("authorizationCode");
        if (authorizationCode != null) {
            flows.setAuthorizationCode(oauthFlowDecoder.decodeAuthorizationCode(
                    authorizationCode,
                    context.child("authorizationCode")));
        }

        AdditionalFieldsMapper.copy(source, flows, MAPPED_FIELDS);
        return flows;
    }
}
