package ru.luttsev.studio.core.model.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.value.DocumentValue;

public final class Discriminator {

    private String propertyName;
    private Map<String, String> mapping = new LinkedHashMap<>();
    private String defaultMapping;
    private Map<String, DocumentValue> extensions = new LinkedHashMap<>();

    public String getPropertyName() {
        return this.propertyName;
    }

    public Map<String, String> getMapping() {
        return this.mapping;
    }

    public String getDefaultMapping() {
        return this.defaultMapping;
    }

    public Map<String, DocumentValue> getExtensions() {
        return this.extensions;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public void setDefaultMapping(String defaultMapping) {
        this.defaultMapping = defaultMapping;
    }

    public void setExtensions(Map<String, DocumentValue> extensions) {
        this.extensions = extensions;
    }

    public Discriminator() {
    }
}
