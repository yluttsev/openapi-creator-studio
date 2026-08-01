package ru.luttsev.studio.core.model.info;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class ExternalDocumentation extends ExtensibleObject {

    private String description;
    private UriReference url;

    public String getDescription() {
        return this.description;
    }

    public UriReference getUrl() {
        return this.url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(UriReference url) {
        this.url = url;
    }

    public ExternalDocumentation() {
    }
}
