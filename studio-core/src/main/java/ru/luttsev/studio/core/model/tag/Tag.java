package ru.luttsev.studio.core.model.tag;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;

public final class Tag extends ExtensibleObject {

    private String name;
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String parent;
    private String kind;

    public String getName() {
        return this.name;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    public String getParent() {
        return this.parent;
    }

    public String getKind() {
        return this.kind;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Tag() {
    }
}
