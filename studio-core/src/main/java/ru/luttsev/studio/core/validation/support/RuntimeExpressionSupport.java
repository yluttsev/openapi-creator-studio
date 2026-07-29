package ru.luttsev.studio.core.validation.support;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.navigation.DocumentPath;

public final class RuntimeExpressionSupport {

    private static final Pattern REQUEST_PARAMETER = Pattern.compile(
            "^\\$request\\.(header|query|path)\\.(.+)$");
    private static final Pattern RESPONSE_HEADER = Pattern.compile(
            "^\\$response\\.header\\.(.+)$");
    private static final Pattern BODY = Pattern.compile(
            "^\\$(request|response)\\.body(?:#(.*))?$");

    private RuntimeExpressionSupport() {
    }

    public static boolean isValid(String expression) {
        if (expression.equals("$url")
                || expression.equals("$method")
                || expression.equals("$statusCode")) {
            return true;
        }

        Matcher requestParameter = REQUEST_PARAMETER.matcher(expression);
        if (requestParameter.matches()) {
            return isPlainName(requestParameter.group(2));
        }

        Matcher responseHeader = RESPONSE_HEADER.matcher(expression);
        if (responseHeader.matches()) {
            return isPlainName(responseHeader.group(1));
        }

        Matcher body = BODY.matcher(expression);
        if (!body.matches()) {
            return false;
        }
        String pointer = body.group(2);
        if (pointer == null || pointer.isEmpty()) {
            return true;
        }
        try {
            DocumentPath.parse(pointer);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean isValidCallbackKey(String callbackKey) {
        return callbackKey.length() >= 3
                && callbackKey.charAt(0) == '{'
                && callbackKey.charAt(callbackKey.length() - 1) == '}'
                && isValid(callbackKey.substring(1, callbackKey.length() - 1));
    }

    public static Optional<RuntimeParameterReference> requestParameter(
            String expression) {
        Matcher matcher = REQUEST_PARAMETER.matcher(expression);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new RuntimeParameterReference(
                toLocation(matcher.group(1)),
                matcher.group(2)));
    }

    public static Optional<String> responseHeader(String expression) {
        Matcher matcher = RESPONSE_HEADER.matcher(expression);
        return matcher.matches()
                ? Optional.of(matcher.group(1))
                : Optional.empty();
    }

    private static ParameterLocation toLocation(String value) {
        return switch (value) {
            case "header" -> ParameterLocation.HEADER;
            case "query" -> ParameterLocation.QUERY;
            case "path" -> ParameterLocation.PATH;
            default -> throw new IllegalArgumentException(
                    "Unsupported runtime parameter location: " + value);
        };
    }

    private static boolean isPlainName(String value) {
        return !value.isBlank()
                && !value.contains("{")
                && !value.contains("}")
                && !value.contains("#");
    }
}
