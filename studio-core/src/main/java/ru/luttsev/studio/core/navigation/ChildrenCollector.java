package ru.luttsev.studio.core.navigation;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.response.ResponseKey;

final class ChildrenCollector {

    private static final Set<String> STANDARD_HTTP_METHODS = Set.of(
            "GET",
            "PUT",
            "POST",
            "DELETE",
            "OPTIONS",
            "HEAD",
            "PATCH",
            "TRACE",
            "QUERY");

    private final LinkedHashMap<String, Object> children = new LinkedHashMap<>();

    void add(String name, Object value) {
        if (value != null) {
            children.put(name, value);
        }
    }

    void addEntries(Map<?, ?> values) {
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (entry.getValue() != null) {
                children.put(segmentFor(entry.getKey()), entry.getValue());
            }
        }
    }

    void addItems(Iterable<?> values) {
        int index = 0;
        for (Object value : values) {
            if (value != null) {
                children.put(Integer.toString(index), value);
            }
            index++;
        }
    }

    void addOperations(Map<HttpMethod, Operation> operations) {
        LinkedHashMap<String, Operation> additionalOperations = new LinkedHashMap<>();
        for (Map.Entry<HttpMethod, Operation> entry : operations.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            String method = entry.getKey().value();
            if (STANDARD_HTTP_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
                children.put(method.toLowerCase(Locale.ROOT), entry.getValue());
            } else {
                additionalOperations.put(method, entry.getValue());
            }
        }
        if (!additionalOperations.isEmpty()) {
            children.put("additionalOperations", additionalOperations);
        }
    }

    void addAdditional(Map<String, ?> additionalValues) {
        for (Map.Entry<String, ?> entry : additionalValues.entrySet()) {
            if (entry.getValue() != null) {
                children.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
    }

    Map<String, Object> result() {
        return children;
    }

    private static String segmentFor(Object key) {
        if (key instanceof HttpMethod method) {
            return method.value().toLowerCase(Locale.ROOT);
        }
        if (key instanceof MediaTypeName mediaTypeName) {
            return mediaTypeName.value();
        }
        if (key instanceof ResponseKey responseKey) {
            return responseKey.value();
        }
        return String.valueOf(key);
    }
}
