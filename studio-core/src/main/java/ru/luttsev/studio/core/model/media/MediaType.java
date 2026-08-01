package ru.luttsev.studio.core.model.media;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.DocumentValue;

public final class MediaType extends ExtensibleObject {

    private Schema schema;
    private Schema itemSchema;
    private DocumentValue example;
    private Map<String, ReferenceOr<Example>> examples = new LinkedHashMap<>();
    private Map<String, Encoding> encoding = new LinkedHashMap<>();
    private List<Encoding> prefixEncoding = new ArrayList<>();
    private Encoding itemEncoding;

    public Schema getSchema() {
        return this.schema;
    }

    public Schema getItemSchema() {
        return this.itemSchema;
    }

    public DocumentValue getExample() {
        return this.example;
    }

    public Map<String, ReferenceOr<Example>> getExamples() {
        return this.examples;
    }

    public Map<String, Encoding> getEncoding() {
        return this.encoding;
    }

    public List<Encoding> getPrefixEncoding() {
        return this.prefixEncoding;
    }

    public Encoding getItemEncoding() {
        return this.itemEncoding;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setItemSchema(Schema itemSchema) {
        this.itemSchema = itemSchema;
    }

    public void setExample(DocumentValue example) {
        this.example = example;
    }

    public void setExamples(Map<String, ReferenceOr<Example>> examples) {
        this.examples = examples;
    }

    public void setEncoding(Map<String, Encoding> encoding) {
        this.encoding = encoding;
    }

    public void setPrefixEncoding(List<Encoding> prefixEncoding) {
        this.prefixEncoding = prefixEncoding;
    }

    public void setItemEncoding(Encoding itemEncoding) {
        this.itemEncoding = itemEncoding;
    }

    public MediaType() {
    }
}
