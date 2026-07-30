package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Set;
import ru.luttsev.studio.core.model.security.ApiKeyLocation;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class SecuritySchemeDecoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "type",
            "description",
            "name",
            "in",
            "scheme",
            "bearerFormat",
            "flows",
            "openIdConnectUrl");

    private final OAuthFlowsDecoder oauthFlowsDecoder = new OAuthFlowsDecoder();

    SecurityScheme decode(ObjectValue source, DecodeContext context) {
        ObjectValueReader reader = new ObjectValueReader(source, context);
        SecurityScheme securityScheme = new SecurityScheme();

        String typeValue = reader.requiredString("type");
        SecuritySchemeType type = SecuritySchemeValueDecoder.type(
                typeValue,
                context.child("type"));
        securityScheme.setType(type);
        securityScheme.setDescription(reader.optionalString("description"));

        String name = type == SecuritySchemeType.API_KEY
                ? reader.requiredString("name")
                : reader.optionalString("name");
        securityScheme.setName(name);

        String locationValue = type == SecuritySchemeType.API_KEY
                ? reader.requiredString("in")
                : reader.optionalString("in");
        ApiKeyLocation location = SecuritySchemeValueDecoder.location(
                locationValue,
                context.child("in"));
        securityScheme.setLocation(location);

        String scheme = type == SecuritySchemeType.HTTP
                ? reader.requiredString("scheme")
                : reader.optionalString("scheme");
        securityScheme.setScheme(scheme);
        securityScheme.setBearerFormat(reader.optionalString("bearerFormat"));

        ObjectValue flowsSource = type == SecuritySchemeType.OAUTH2
                ? reader.requiredObject("flows")
                : reader.optionalObject("flows");
        if (flowsSource != null) {
            OAuthFlows flows = oauthFlowsDecoder.decode(
                    flowsSource,
                    context.child("flows"));
            securityScheme.setFlows(flows);
        }

        securityScheme.setOpenIdConnectUrl(
                type == SecuritySchemeType.OPEN_ID_CONNECT
                        ? reader.requiredUriReference("openIdConnectUrl")
                        : reader.optionalUriReference("openIdConnectUrl"));

        AdditionalFieldsMapper.copy(source, securityScheme, MAPPED_FIELDS);
        return securityScheme;
    }
}
