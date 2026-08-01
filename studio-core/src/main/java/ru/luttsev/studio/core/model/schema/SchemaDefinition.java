package ru.luttsev.studio.core.model.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.reference.ReferenceHolder;
import ru.luttsev.studio.core.model.value.DocumentValue;

public final class SchemaDefinition implements Schema, ReferenceHolder {

    private Set<JsonType> types = new LinkedHashSet<>();
    private SchemaFormat format;
    private UriReference ref;

    private String title;
    private String description;
    private DocumentValue defaultValue;
    private List<DocumentValue> examples = new ArrayList<>();
    private List<DocumentValue> enumValues = new ArrayList<>();
    private DocumentValue constValue;
    private Boolean deprecated;
    private Boolean readOnly;
    private Boolean writeOnly;

    private Map<String, Schema> properties = new LinkedHashMap<>();
    private Set<String> required = new LinkedHashSet<>();
    private Schema additionalProperties;
    private BigInteger minProperties;
    private BigInteger maxProperties;

    private Schema items;
    private BigInteger minItems;
    private BigInteger maxItems;
    private Boolean uniqueItems;

    private BigInteger minLength;
    private BigInteger maxLength;
    private String pattern;

    private BigDecimal minimum;
    private BigDecimal maximum;
    private BigDecimal exclusiveMinimum;
    private BigDecimal exclusiveMaximum;
    private BigDecimal multipleOf;

    private List<Schema> allOf = new ArrayList<>();
    private List<Schema> anyOf = new ArrayList<>();
    private List<Schema> oneOf = new ArrayList<>();
    private Schema notSchema;

    private Discriminator discriminator;
    private Map<String, DocumentValue> additionalKeywords = new LinkedHashMap<>();

    public Set<JsonType> getTypes() {
        return this.types;
    }

    public SchemaFormat getFormat() {
        return this.format;
    }

    public UriReference getRef() {
        return this.ref;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public DocumentValue getDefaultValue() {
        return this.defaultValue;
    }

    public List<DocumentValue> getExamples() {
        return this.examples;
    }

    public List<DocumentValue> getEnumValues() {
        return this.enumValues;
    }

    public DocumentValue getConstValue() {
        return this.constValue;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public Boolean getReadOnly() {
        return this.readOnly;
    }

    public Boolean getWriteOnly() {
        return this.writeOnly;
    }

    public Map<String, Schema> getProperties() {
        return this.properties;
    }

    public Set<String> getRequired() {
        return this.required;
    }

    public Schema getAdditionalProperties() {
        return this.additionalProperties;
    }

    public BigInteger getMinProperties() {
        return this.minProperties;
    }

    public BigInteger getMaxProperties() {
        return this.maxProperties;
    }

    public Schema getItems() {
        return this.items;
    }

    public BigInteger getMinItems() {
        return this.minItems;
    }

    public BigInteger getMaxItems() {
        return this.maxItems;
    }

    public Boolean getUniqueItems() {
        return this.uniqueItems;
    }

    public BigInteger getMinLength() {
        return this.minLength;
    }

    public BigInteger getMaxLength() {
        return this.maxLength;
    }

    public String getPattern() {
        return this.pattern;
    }

    public BigDecimal getMinimum() {
        return this.minimum;
    }

    public BigDecimal getMaximum() {
        return this.maximum;
    }

    public BigDecimal getExclusiveMinimum() {
        return this.exclusiveMinimum;
    }

    public BigDecimal getExclusiveMaximum() {
        return this.exclusiveMaximum;
    }

    public BigDecimal getMultipleOf() {
        return this.multipleOf;
    }

    public List<Schema> getAllOf() {
        return this.allOf;
    }

    public List<Schema> getAnyOf() {
        return this.anyOf;
    }

    public List<Schema> getOneOf() {
        return this.oneOf;
    }

    public Schema getNotSchema() {
        return this.notSchema;
    }

    public Discriminator getDiscriminator() {
        return this.discriminator;
    }

    public Map<String, DocumentValue> getAdditionalKeywords() {
        return this.additionalKeywords;
    }

    public void setTypes(Set<JsonType> types) {
        this.types = types;
    }

    public void setFormat(SchemaFormat format) {
        this.format = format;
    }

    public void setRef(UriReference ref) {
        this.ref = ref;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultValue(DocumentValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setExamples(List<DocumentValue> examples) {
        this.examples = examples;
    }

    public void setEnumValues(List<DocumentValue> enumValues) {
        this.enumValues = enumValues;
    }

    public void setConstValue(DocumentValue constValue) {
        this.constValue = constValue;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setWriteOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }

    public void setRequired(Set<String> required) {
        this.required = required;
    }

    public void setAdditionalProperties(Schema additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void setMinProperties(BigInteger minProperties) {
        this.minProperties = minProperties;
    }

    public void setMaxProperties(BigInteger maxProperties) {
        this.maxProperties = maxProperties;
    }

    public void setItems(Schema items) {
        this.items = items;
    }

    public void setMinItems(BigInteger minItems) {
        this.minItems = minItems;
    }

    public void setMaxItems(BigInteger maxItems) {
        this.maxItems = maxItems;
    }

    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public void setMinLength(BigInteger minLength) {
        this.minLength = minLength;
    }

    public void setMaxLength(BigInteger maxLength) {
        this.maxLength = maxLength;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setMinimum(BigDecimal minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(BigDecimal maximum) {
        this.maximum = maximum;
    }

    public void setExclusiveMinimum(BigDecimal exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    public void setExclusiveMaximum(BigDecimal exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    public void setMultipleOf(BigDecimal multipleOf) {
        this.multipleOf = multipleOf;
    }

    public void setAllOf(List<Schema> allOf) {
        this.allOf = allOf;
    }

    public void setAnyOf(List<Schema> anyOf) {
        this.anyOf = anyOf;
    }

    public void setOneOf(List<Schema> oneOf) {
        this.oneOf = oneOf;
    }

    public void setNotSchema(Schema notSchema) {
        this.notSchema = notSchema;
    }

    public void setDiscriminator(Discriminator discriminator) {
        this.discriminator = discriminator;
    }

    public void setAdditionalKeywords(Map<String, DocumentValue> additionalKeywords) {
        this.additionalKeywords = additionalKeywords;
    }

    public SchemaDefinition() {
    }
}
