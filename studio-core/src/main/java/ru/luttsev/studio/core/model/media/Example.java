package ru.luttsev.studio.core.model.media;

import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.DocumentValue;

public final class Example extends ExtensibleObject {

    private String summary;
    private String description;
    private DocumentValue dataValue;
    private String serializedValue;
    private UriReference externalValue;
    private DocumentValue value;

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public DocumentValue getDataValue() {
        return this.dataValue;
    }

    public String getSerializedValue() {
        return this.serializedValue;
    }

    public UriReference getExternalValue() {
        return this.externalValue;
    }

    public DocumentValue getValue() {
        return this.value;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDataValue(DocumentValue dataValue) {
        this.dataValue = dataValue;
    }

    public void setSerializedValue(String serializedValue) {
        this.serializedValue = serializedValue;
    }

    public void setExternalValue(UriReference externalValue) {
        this.externalValue = externalValue;
    }

    public void setValue(DocumentValue value) {
        this.value = value;
    }

    public Example() {
    }
}
