package ru.luttsev.studio.core.navigation;

import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.security.SecurityScheme;

final class SecurityMappings {

    private SecurityMappings() {
    }

    static void register(DocumentNodeRegistry registry) {
        registry.register(
                SecurityScheme.class,
                SecurityMappings::collectSecurityScheme);
        registry.register(OAuthFlows.class, SecurityMappings::collectOAuthFlows);
        registry.register(OAuthFlow.class, SecurityMappings::collectOAuthFlow);
        registry.register(
                SecurityRequirement.class,
                SecurityMappings::collectSecurityRequirement);
    }

    private static void collectSecurityScheme(
            SecurityScheme securityScheme,
            ChildrenCollector children) {
        children.add("type", securityScheme.getType());
        children.add("description", securityScheme.getDescription());
        children.add("name", securityScheme.getName());
        children.add("in", securityScheme.getLocation());
        children.add("scheme", securityScheme.getScheme());
        children.add("bearerFormat", securityScheme.getBearerFormat());
        children.add("flows", securityScheme.getFlows());
        children.add("openIdConnectUrl", securityScheme.getOpenIdConnectUrl());
        children.add("oauth2MetadataUrl", securityScheme.getOauth2MetadataUrl());
        children.add("deprecated", securityScheme.getDeprecated());
    }

    private static void collectOAuthFlows(
            OAuthFlows flows,
            ChildrenCollector children) {
        children.add("implicit", flows.getImplicit());
        children.add("password", flows.getPassword());
        children.add("clientCredentials", flows.getClientCredentials());
        children.add("authorizationCode", flows.getAuthorizationCode());
        children.add("deviceAuthorization", flows.getDeviceAuthorization());
    }

    private static void collectOAuthFlow(
            OAuthFlow flow,
            ChildrenCollector children) {
        children.add("authorizationUrl", flow.getAuthorizationUrl());
        children.add("deviceAuthorizationUrl", flow.getDeviceAuthorizationUrl());
        children.add("tokenUrl", flow.getTokenUrl());
        children.add("refreshUrl", flow.getRefreshUrl());
        children.add("scopes", flow.getScopes());
    }

    private static void collectSecurityRequirement(
            SecurityRequirement requirement,
            ChildrenCollector children) {
        children.addEntries(requirement.getRequirements());
    }
}
