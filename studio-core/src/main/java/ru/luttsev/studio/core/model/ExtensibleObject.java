package ru.luttsev.studio.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.value.DocumentValue;

public abstract class ExtensibleObject {

    private final Map<String, DocumentValue> additionalFields = new LinkedHashMap<>();

    public Map<String, DocumentValue> getAdditionalFields() {
        return this.additionalFields;
    }
}
