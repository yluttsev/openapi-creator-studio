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

public final class Header extends ExtensibleObject {

    private String description;
    private Boolean required;
    private Boolean deprecated;
    private ParameterStyle style;
    private Boolean explode;
    private Schema schema;
    private DocumentValue example;
    private Map<String, ReferenceOr<Example>> examples = new LinkedHashMap<>();
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();

    public String getDescription() {
        return this.description;
    }

    public Boolean getRequired() {
        return this.required;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public ParameterStyle getStyle() {
        return this.style;
    }

    public Boolean getExplode() {
        return this.explode;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    public void setExplode(Boolean explode) {
        this.explode = explode;
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

    public Header() {
    }
}
