package ru.luttsev.studio.core.command.component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import ru.luttsev.studio.core.model.Components;

final class ComponentCollectionResolver {

    private ComponentCollectionResolver() {
    }

    static Optional<Map<String, ?>> resolve(
            Components components,
            String section) {
        if (components == null) {
            return Optional.empty();
        }
        return switch (section) {
            case "schemas" -> Optional.ofNullable(components.getSchemas());
            case "responses" -> Optional.ofNullable(components.getResponses());
            case "parameters" -> Optional.ofNullable(components.getParameters());
            case "examples" -> Optional.ofNullable(components.getExamples());
            case "requestBodies" -> Optional.ofNullable(components.getRequestBodies());
            case "headers" -> Optional.ofNullable(components.getHeaders());
            case "securitySchemes" ->
                    Optional.ofNullable(components.getSecuritySchemes());
            case "links" -> Optional.ofNullable(components.getLinks());
            case "callbacks" -> Optional.ofNullable(components.getCallbacks());
            case "pathItems" -> Optional.ofNullable(components.getPathItems());
            case "mediaTypes" -> Optional.ofNullable(components.getMediaTypes());
            default -> Optional.empty();
        };
    }

    static <T> void renameKey(
            Map<String, T> values,
            String currentName,
            String newName) {
        LinkedHashMap<String, T> renamed = new LinkedHashMap<>();
        for (Map.Entry<String, T> entry : values.entrySet()) {
            String name = entry.getKey().equals(currentName)
                    ? newName
                    : entry.getKey();
            renamed.put(name, entry.getValue());
        }
        values.clear();
        values.putAll(renamed);
    }
}
