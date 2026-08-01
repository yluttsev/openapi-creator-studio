package ru.luttsev.studio.core.model.security;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class SecurityScheme extends ExtensibleObject {

    private SecuritySchemeType type;
    private String description;
    private String name;
    private ApiKeyLocation location;
    private String scheme;
    private String bearerFormat;
    private OAuthFlows flows;
    private UriReference openIdConnectUrl;
    private UriReference oauth2MetadataUrl;
    private Boolean deprecated;

    public SecuritySchemeType getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public ApiKeyLocation getLocation() {
        return this.location;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getBearerFormat() {
        return this.bearerFormat;
    }

    public OAuthFlows getFlows() {
        return this.flows;
    }

    public UriReference getOpenIdConnectUrl() {
        return this.openIdConnectUrl;
    }

    public UriReference getOauth2MetadataUrl() {
        return this.oauth2MetadataUrl;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public void setType(SecuritySchemeType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(ApiKeyLocation location) {
        this.location = location;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setBearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
    }

    public void setFlows(OAuthFlows flows) {
        this.flows = flows;
    }

    public void setOpenIdConnectUrl(UriReference openIdConnectUrl) {
        this.openIdConnectUrl = openIdConnectUrl;
    }

    public void setOauth2MetadataUrl(UriReference oauth2MetadataUrl) {
        this.oauth2MetadataUrl = oauth2MetadataUrl;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public SecurityScheme() {
    }
}
