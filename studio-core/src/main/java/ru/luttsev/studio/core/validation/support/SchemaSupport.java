package ru.luttsev.studio.core.validation.support;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class SchemaSupport {

    private SchemaSupport() {
    }

    public static Optional<SchemaDefinition> resolveDefinition(
            ValidationContext context,
            Schema schema) {
        Set<Schema> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Schema current = schema;
        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        while (current instanceof SchemaDefinition definition) {
            if (!visited.add(current)) {
                return Optional.empty();
            }
            if (definition.getRef() == null) {
                return Optional.of(definition);
            }
            Optional<Schema> target = resolver.resolve(definition.getRef(), Schema.class);
            if (target.isEmpty()) {
                return Optional.empty();
            }
            current = target.orElseThrow();
        }
        return Optional.empty();
    }

    public static boolean isRequired(
            ValidationContext context,
            SchemaDefinition schema,
            String propertyName) {
        Set<SchemaDefinition> visited =
                Collections.newSetFromMap(new IdentityHashMap<>());
        return isRequired(context, schema, propertyName, visited);
    }

    private static boolean isRequired(
            ValidationContext context,
            SchemaDefinition schema,
            String propertyName,
            Set<SchemaDefinition> visited) {
        if (!visited.add(schema)) {
            return false;
        }
        if (schema.getRequired().contains(propertyName)) {
            return true;
        }
        for (Schema composedSchema : schema.getAllOf()) {
            Optional<SchemaDefinition> definition =
                    resolveDefinition(context, composedSchema);
            if (definition.isPresent()
                    && isRequired(
                            context,
                            definition.orElseThrow(),
                            propertyName,
                            visited)) {
                return true;
            }
        }
        return false;
    }
}
