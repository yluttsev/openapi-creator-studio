package ru.luttsev.studio.openapi.version.v31.encode;

import ru.luttsev.studio.core.model.security.ApiKeyLocation;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;

final class SecuritySchemeValueEncoder {

    private SecuritySchemeValueEncoder() {
    }

    static String type(SecuritySchemeType value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case API_KEY -> "apiKey";
            case HTTP -> "http";
            case MUTUAL_TLS -> "mutualTLS";
            case OAUTH2 -> "oauth2";
            case OPEN_ID_CONNECT -> "openIdConnect";
        };
    }

    static String location(ApiKeyLocation value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case QUERY -> "query";
            case HEADER -> "header";
            case COOKIE -> "cookie";
        };
    }
}
