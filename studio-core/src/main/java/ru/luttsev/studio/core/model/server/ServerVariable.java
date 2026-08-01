package ru.luttsev.studio.core.model.server;

import java.util.ArrayList;
import java.util.List;
import ru.luttsev.studio.core.model.ExtensibleObject;

public final class ServerVariable extends ExtensibleObject {

    private List<String> enumValues = new ArrayList<>();
    private String defaultValue;
    private String description;

    public List<String> getEnumValues() {
        return this.enumValues;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public String getDescription() {
        return this.description;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ServerVariable() {
    }
}
