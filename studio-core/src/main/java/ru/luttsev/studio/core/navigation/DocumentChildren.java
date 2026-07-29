package ru.luttsev.studio.core.navigation;

import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.ObjectValue;

final class DocumentChildren {

    private static final DocumentNodeRegistry REGISTRY =
            DocumentNodeRegistry.createDefault();

    private DocumentChildren() {
    }

    static Map<String, Object> of(Object value) {
        ChildrenCollector children = new ChildrenCollector();

        if (value instanceof Map<?, ?> map) {
            children.addEntries(map);
        } else if (value instanceof Iterable<?> iterable) {
            children.addItems(iterable);
        } else if (value instanceof ObjectValue objectValue) {
            children.addEntries(objectValue.values());
        } else if (value instanceof ArrayValue arrayValue) {
            children.addItems(arrayValue.values());
        } else {
            REGISTRY.collect(value, children);
        }

        if (value instanceof ExtensibleObject extensibleObject) {
            children.addAdditional(extensibleObject.getAdditionalFields());
        }
        return children.result();
    }
}
