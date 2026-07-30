package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.Map;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.parameter.ParameterStyle;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ParameterValueDecoder {

    private static final Map<String, ParameterLocation> LOCATIONS = Map.of(
            "query", ParameterLocation.QUERY,
            "header", ParameterLocation.HEADER,
            "path", ParameterLocation.PATH,
            "cookie", ParameterLocation.COOKIE);

    private static final Map<String, ParameterStyle> STYLES = Map.of(
            "matrix", ParameterStyle.MATRIX,
            "label", ParameterStyle.LABEL,
            "form", ParameterStyle.FORM,
            "simple", ParameterStyle.SIMPLE,
            "spaceDelimited", ParameterStyle.SPACE_DELIMITED,
            "pipeDelimited", ParameterStyle.PIPE_DELIMITED,
            "deepObject", ParameterStyle.DEEP_OBJECT);

    private ParameterValueDecoder() {
    }

    static ParameterLocation location(
            String value,
            DecodeContext context) {
        return decode(
                value,
                LOCATIONS,
                "parameter location",
                context);
    }

    static ParameterStyle style(
            String value,
            DecodeContext context) {
        return decode(
                value,
                STYLES,
                "parameter style",
                context);
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
