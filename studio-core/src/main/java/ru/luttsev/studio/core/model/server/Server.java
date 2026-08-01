package ru.luttsev.studio.core.model.server;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;

public final class Server extends ExtensibleObject {

    private String url;
    private String description;
    private String name;
    private Map<String, ServerVariable> variables = new LinkedHashMap<>();

    public String getUrl() {
        return this.url;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, ServerVariable> getVariables() {
        return this.variables;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVariables(Map<String, ServerVariable> variables) {
        this.variables = variables;
    }

    public Server() {
    }
}
