package ru.luttsev.studio.core.model.parameter;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.DocumentValue;

public final class Parameter extends ExtensibleObject {

    private String name;
    private ParameterLocation location;
    private String description;
    private Boolean required;
    private Boolean deprecated;
    private Boolean allowEmptyValue;
    private ParameterStyle style;
    private Boolean explode;
    private Boolean allowReserved;
    private Schema schema;
    private DocumentValue example;
    private Map<String, ReferenceOr<Example>> examples = new LinkedHashMap<>();
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();

    public String getName() {
        return this.name;
    }

    public ParameterLocation getLocation() {
        return this.location;
    }

    public String getDescription() {
        return this.description;
    }

    public Boolean getRequired() {
        return this.required;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public Boolean getAllowEmptyValue() {
        return this.allowEmptyValue;
    }

    public ParameterStyle getStyle() {
        return this.style;
    }

    public Boolean getExplode() {
        return this.explode;
    }

    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    public Schema getSchema() {
        return this.schema;
    }

    public DocumentValue getExample() {
        return this.example;
    }

    public Map<String, ReferenceOr<Example>> getExamples() {
        return this.examples;
    }

    public Map<MediaTypeName, ReferenceOr<MediaType>> getContent() {
        return this.content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(ParameterLocation location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setExample(DocumentValue example) {
        this.example = example;
    }

    public void setExamples(Map<String, ReferenceOr<Example>> examples) {
        this.examples = examples;
    }

    public void setContent(Map<MediaTypeName, ReferenceOr<MediaType>> content) {
        this.content = content;
    }

    public Parameter() {
    }
}
