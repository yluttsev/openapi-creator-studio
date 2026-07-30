package ru.luttsev.studio.openapi.validation.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record StructuralSchemaBundle(
        String rootSchemaId,
        Map<String, String> resources) {

    public StructuralSchemaBundle {
        Objects.requireNonNull(rootSchemaId, "rootSchemaId must not be null");
        Objects.requireNonNull(resources, "resources must not be null");
        if (rootSchemaId.isBlank()) {
            throw new IllegalArgumentException("rootSchemaId must not be blank");
        }

        LinkedHashMap<String, String> resourceCopy = new LinkedHashMap<>();
        for (Map.Entry<String, String> resource : resources.entrySet()) {
            String id = Objects.requireNonNull(
                    resource.getKey(),
                    "schema resource id must not be null");
            String content = Objects.requireNonNull(
                    resource.getValue(),
                    "schema resource content must not be null");
            if (id.isBlank()) {
                throw new IllegalArgumentException(
                        "schema resource id must not be blank");
            }
            if (content.isBlank()) {
                throw new IllegalArgumentException(
                        "schema resource content must not be blank");
            }
            resourceCopy.put(id, content);
        }
        if (!resourceCopy.containsKey(rootSchemaId)) {
            throw new IllegalArgumentException(
                    "resources must contain the root schema");
        }
        resources = Map.copyOf(resourceCopy);
    }
}
