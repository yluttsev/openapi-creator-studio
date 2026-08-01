package ru.luttsev.studio.openapi.version.v31.encode;

import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.parameter.ParameterStyle;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ParameterValueEncoder {

    private ParameterValueEncoder() {
    }

    static String location(
            ParameterLocation value,
            EncodeContext context) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case QUERY -> "query";
            case HEADER -> "header";
            case PATH -> "path";
            case COOKIE -> "cookie";
            case QUERYSTRING -> {
                context.error(
                        OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                        "Parameter location 'querystring' is not supported by OpenAPI "
                                + context.targetVersion().value());
                yield null;
            }
        };
    }

    static String style(ParameterStyle value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case MATRIX -> "matrix";
            case LABEL -> "label";
            case FORM -> "form";
            case SIMPLE -> "simple";
            case SPACE_DELIMITED -> "spaceDelimited";
            case PIPE_DELIMITED -> "pipeDelimited";
            case DEEP_OBJECT -> "deepObject";
        };
    }
}
