package ru.luttsev.studio.core.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class DocumentNodeRegistry {

    private final Map<Class<?>, RegisteredMapping> mappings = new HashMap<>();

    static DocumentNodeRegistry createDefault() {
        DocumentNodeRegistry registry = new DocumentNodeRegistry();
        RootMappings.register(registry);
        PathMappings.register(registry);
        PayloadMappings.register(registry);
        SecurityMappings.register(registry);
        SchemaMappings.register(registry);
        return registry;
    }

    <T> void register(Class<T> nodeType, NodeMapping<T> mapping) {
        Objects.requireNonNull(nodeType, "nodeType must not be null");
        Objects.requireNonNull(mapping, "mapping must not be null");

        RegisteredMapping previous = mappings.put(
                nodeType,
                (node, children) -> mapping.collect(nodeType.cast(node), children));
        if (previous != null) {
            throw new IllegalStateException(
                    "Mapping is already registered for " + nodeType.getName());
        }
    }

    void collect(Object node, ChildrenCollector children) {
        RegisteredMapping mapping = mappings.get(node.getClass());
        if (mapping != null) {
            mapping.collect(node, children);
        }
    }

    @FunctionalInterface
    private interface RegisteredMapping {

        void collect(Object node, ChildrenCollector children);
    }
}
