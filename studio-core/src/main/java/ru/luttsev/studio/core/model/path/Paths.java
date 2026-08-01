package ru.luttsev.studio.core.model.path;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;

public final class Paths extends ExtensibleObject {

    private final Map<String, PathItem> items = new LinkedHashMap<>();

    public Map<String, PathItem> getItems() {
        return this.items;
    }
}
