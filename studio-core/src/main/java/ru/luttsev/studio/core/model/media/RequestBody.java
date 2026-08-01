package ru.luttsev.studio.core.model.media;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

public final class RequestBody extends ExtensibleObject {

    private String description;
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();
    private Boolean required;

    public String getDescription() {
        return this.description;
    }

    public Map<MediaTypeName, ReferenceOr<MediaType>> getContent() {
        return this.content;
    }

    public Boolean getRequired() {
        return this.required;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(Map<MediaTypeName, ReferenceOr<MediaType>> content) {
        this.content = content;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public RequestBody() {
    }
}
