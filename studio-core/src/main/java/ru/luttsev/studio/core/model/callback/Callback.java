package ru.luttsev.studio.core.model.callback;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.path.PathItem;

public final class Callback extends ExtensibleObject {

    private final Map<String, PathItem> expressions = new LinkedHashMap<>();

    public Map<String, PathItem> getExpressions() {
        return this.expressions;
    }
}
