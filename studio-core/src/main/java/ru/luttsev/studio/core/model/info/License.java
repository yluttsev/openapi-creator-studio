package ru.luttsev.studio.core.model.info;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class License extends ExtensibleObject {

    private String name;
    private String identifier;
    private UriReference url;

    public String getName() {
        return this.name;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public UriReference getUrl() {
        return this.url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setUrl(UriReference url) {
        this.url = url;
    }

    public License() {
    }
}
