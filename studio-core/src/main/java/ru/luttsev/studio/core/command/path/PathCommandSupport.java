package ru.luttsev.studio.core.command.path;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.navigation.DocumentPath;

final class PathCommandSupport {

    private static final Set<String> STANDARD_METHODS = Set.of(
            "GET",
            "PUT",
            "POST",
            "DELETE",
            "OPTIONS",
            "HEAD",
            "PATCH",
            "TRACE",
            "QUERY");

    private PathCommandSupport() {
    }

    static DocumentPath pathPath(String pathTemplate) {
        return DocumentPath.root()
                .child("paths")
                .child(pathTemplate);
    }

    static HttpMethod canonicalMethod(HttpMethod method) {
        String value = method.value();
        String upperCaseValue = value.toUpperCase(Locale.ROOT);
        return STANDARD_METHODS.contains(upperCaseValue)
                ? new HttpMethod(upperCaseValue)
                : method;
    }

    static Optional<HttpMethod> findStoredMethod(
            Map<HttpMethod, ?> operations,
            HttpMethod method) {
        HttpMethod canonicalMethod = canonicalMethod(method);
        return operations.keySet().stream()
                .filter(storedMethod ->
                        canonicalMethod(storedMethod).equals(canonicalMethod))
                .findFirst();
    }

    static DocumentPath operationPath(
            String pathTemplate,
            HttpMethod method) {
        String methodName = method.value();
        if (STANDARD_METHODS.contains(methodName.toUpperCase(Locale.ROOT))) {
            return pathPath(pathTemplate)
                    .child(methodName.toLowerCase(Locale.ROOT));
        }
        return pathPath(pathTemplate)
                .child("additionalOperations")
                .child(methodName);
    }
}
