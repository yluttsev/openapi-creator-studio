package ru.luttsev.studio.core.model.security;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class OAuthFlow extends ExtensibleObject {

    private UriReference authorizationUrl;
    private UriReference deviceAuthorizationUrl;
    private UriReference tokenUrl;
    private UriReference refreshUrl;
    private Map<String, String> scopes = new LinkedHashMap<>();

    public UriReference getAuthorizationUrl() {
        return this.authorizationUrl;
    }

    public UriReference getDeviceAuthorizationUrl() {
        return this.deviceAuthorizationUrl;
    }

    public UriReference getTokenUrl() {
        return this.tokenUrl;
    }

    public UriReference getRefreshUrl() {
        return this.refreshUrl;
    }

    public Map<String, String> getScopes() {
        return this.scopes;
    }

    public void setAuthorizationUrl(UriReference authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public void setDeviceAuthorizationUrl(UriReference deviceAuthorizationUrl) {
        this.deviceAuthorizationUrl = deviceAuthorizationUrl;
    }

    public void setTokenUrl(UriReference tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public void setRefreshUrl(UriReference refreshUrl) {
        this.refreshUrl = refreshUrl;
    }

    public void setScopes(Map<String, String> scopes) {
        this.scopes = scopes;
    }

    public OAuthFlow() {
    }
}
