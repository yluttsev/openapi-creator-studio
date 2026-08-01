package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.Set;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class SecuritySchemeEncoder {

    private static final Set<String> MAPPED_FIELDS = Set.of(
            "type",
            "description",
            "name",
            "in",
            "scheme",
            "bearerFormat",
            "flows",
            "openIdConnectUrl",
            "oauth2MetadataUrl",
            "deprecated");

    private final OAuthFlowsEncoder oauthFlowsEncoder =
            new OAuthFlowsEncoder();

    ObjectValue encode(SecurityScheme source, EncodeContext context) {
        ObjectValueBuilder target = new ObjectValueBuilder()
                .putString(
                        "type",
                        SecuritySchemeValueEncoder.type(source.getType()))
                .putString("description", source.getDescription())
                .putString("name", source.getName())
                .putString(
                        "in",
                        SecuritySchemeValueEncoder.location(
                                source.getLocation()))
                .putString("scheme", source.getScheme())
                .putString("bearerFormat", source.getBearerFormat());
        if (source.getFlows() != null) {
            target.put(
                    "flows",
                    oauthFlowsEncoder.encode(
                            source.getFlows(),
                            context.child("flows")));
        }
        if (source.getOpenIdConnectUrl() != null) {
            target.putString(
                    "openIdConnectUrl",
                    source.getOpenIdConnectUrl().value());
        }
        if (source.getOauth2MetadataUrl() != null) {
            context.unsupportedField("oauth2MetadataUrl");
        }
        if (source.getDeprecated() != null) {
            context.unsupportedField("deprecated");
        }
        AdditionalFieldsEncoder.copy(
                source,
                target,
                MAPPED_FIELDS,
                context);
        return target.build();
    }
}
