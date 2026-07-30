package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class OAuthFlowDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "authorizationUrl",
            "tokenUrl",
            "refreshUrl",
            "scopes");

    OAuthFlow decodeImplicit(
            ObjectValue source,
            DecodeContext context) {
        return decode(source, context, true, false);
    }

    OAuthFlow decodePassword(
            ObjectValue source,
            DecodeContext context) {
        return decode(source, context, false, true);
    }

    OAuthFlow decodeClientCredentials(
            ObjectValue source,
            DecodeContext context) {
        return decode(source, context, false, true);
    }

    OAuthFlow decodeAuthorizationCode(
            ObjectValue source,
            DecodeContext context) {
        return decode(source, context, true, true);
    }

    private OAuthFlow decode(
            ObjectValue source,
            DecodeContext context,
            boolean authorizationUrlRequired,
            boolean tokenUrlRequired) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        OAuthFlow flow = new OAuthFlow();
        flow.setAuthorizationUrl(authorizationUrlRequired
                ? reader.requiredUriReference("authorizationUrl")
                : reader.optionalUriReference("authorizationUrl"));
        flow.setTokenUrl(tokenUrlRequired
                ? reader.requiredUriReference("tokenUrl")
                : reader.optionalUriReference("tokenUrl"));
        flow.setRefreshUrl(reader.optionalUriReference("refreshUrl"));
        flow.setScopes(decodeScopes(
                reader.requiredObject("scopes"),
                context.child("scopes")));
        AdditionalFieldsMapper.copy(source, flow, MAPPED_FIELDS);
        return flow;
    }

    private static Map<String, String> decodeScopes(
            ObjectValue source,
            DecodeContext context) {
        LinkedHashMap<String, String> scopes = new LinkedHashMap<>();
        if (source == null) {
            return scopes;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            if (entry.getValue() instanceof StringValue stringValue) {
                scopes.put(entry.getKey(), stringValue.value());
            } else {
                context.child(entry.getKey()).error(
                        OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                        "Expected string but found "
                                + ObjectValueReader.typeOf(entry.getValue()));
            }
        }
        return scopes;
    }
}
