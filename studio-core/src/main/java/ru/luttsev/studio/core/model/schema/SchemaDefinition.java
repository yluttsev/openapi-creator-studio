package ru.luttsev.studio.core.model.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
@Setter
@NoArgsConstructor
public final class SchemaDefinition implements Schema {

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
}
