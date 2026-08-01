package ru.luttsev.studio.core.model.reference;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class ReferenceObject<T> extends ExtensibleObject
        implements ReferenceOr<T>, ReferenceHolder {

    private UriReference ref;
    private String summary;
    private String description;

    public UriReference getRef() {
        return this.ref;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public void setRef(UriReference ref) {
        this.ref = ref;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReferenceObject() {
    }
}
