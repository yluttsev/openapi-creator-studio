package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import ru.luttsev.studio.core.model.security.ApiKeyLocation;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class SecuritySchemeValueDecoder {

    private static final Map<String, SecuritySchemeType> TYPES = Map.of(
            "apiKey", SecuritySchemeType.API_KEY,
            "http", SecuritySchemeType.HTTP,
            "mutualTLS", SecuritySchemeType.MUTUAL_TLS,
            "oauth2", SecuritySchemeType.OAUTH2,
            "openIdConnect", SecuritySchemeType.OPEN_ID_CONNECT);

    private static final Map<String, ApiKeyLocation> LOCATIONS = Map.of(
            "query", ApiKeyLocation.QUERY,
            "header", ApiKeyLocation.HEADER,
            "cookie", ApiKeyLocation.COOKIE);

    private SecuritySchemeValueDecoder() {
    }

    static SecuritySchemeType type(
            String value,
            DecodeContext context) {
        return decode(value, TYPES, "security scheme type", context);
    }

    static ApiKeyLocation location(
            String value,
            DecodeContext context) {
        return decode(value, LOCATIONS, "API key location", context);
    }

    private static <T> T decode(
            String value,
            Map<String, T> values,
            String valueName,
            DecodeContext context) {
        if (value == null) {
            return null;
        }

        T decoded = values.get(value);
        if (decoded == null) {
            context.error(
                    OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                    "Unsupported " + valueName + " '" + value + "'");
        }
        return decoded;
    }
}
