package ru.luttsev.studio.core.model.security;

import ru.luttsev.studio.core.model.ExtensibleObject;

public final class OAuthFlows extends ExtensibleObject {

    private OAuthFlow implicit;
    private OAuthFlow password;
    private OAuthFlow clientCredentials;
    private OAuthFlow authorizationCode;
    private OAuthFlow deviceAuthorization;

    public OAuthFlow getImplicit() {
        return this.implicit;
    }

    public OAuthFlow getPassword() {
        return this.password;
    }

    public OAuthFlow getClientCredentials() {
        return this.clientCredentials;
    }

    public OAuthFlow getAuthorizationCode() {
        return this.authorizationCode;
    }

    public OAuthFlow getDeviceAuthorization() {
        return this.deviceAuthorization;
    }

    public void setImplicit(OAuthFlow implicit) {
        this.implicit = implicit;
    }

    public void setPassword(OAuthFlow password) {
        this.password = password;
    }

    public void setClientCredentials(OAuthFlow clientCredentials) {
        this.clientCredentials = clientCredentials;
    }

    public void setAuthorizationCode(OAuthFlow authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public void setDeviceAuthorization(OAuthFlow deviceAuthorization) {
        this.deviceAuthorization = deviceAuthorization;
    }

    public OAuthFlows() {
    }
}
