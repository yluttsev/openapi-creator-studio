package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.LogicalSchema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.SchemaFormat;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;

class SchemaEncoderTest {

    private final SchemaEncoder encoder = new SchemaEncoder();

    @Test
    void encodesLogicalSchemas() {
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2);

        DocumentValue allowed = encoder.encode(
                new LogicalSchema(true),
                context);
        DocumentValue denied = encoder.encode(
                new LogicalSchema(false),
                context);

        assertEquals(new BooleanValue(true), allowed);
        assertEquals(new BooleanValue(false), denied);
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void encodesSchemaDefinitionRecursively() {
        SchemaDefinition source = comprehensiveSchema();
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("components")
                .child("schemas")
                .child("User");

        DocumentValue encoded = encoder.encode(source, context);

        ObjectValue expected = parseSchema("""
                type: [object, "null"]
                format: aggregate
                $ref: "#/components/schemas/Base"
                title: User
                description: User aggregate
                default: active
                examples: [first, second]
                enum: [active, inactive]
                const: active
                deprecated: true
                readOnly: false
                writeOnly: true
                properties:
                  id:
                    type: string
                    minLength: 1
                    maxLength: 36
                    pattern: "^[a-z]+$"
                required: [id]
                additionalProperties: false
                minProperties: 1
                maxProperties: 8
                items: true
                minItems: 1
                maxItems: 5
                uniqueItems: true
                minLength: 2
                maxLength: 64
                pattern: "^[A-Z]"
                minimum: 0
                maximum: 100
                exclusiveMinimum: -1
                exclusiveMaximum: 101
                multipleOf: 0.5
                allOf:
                  - $ref: "#/components/schemas/Audited"
                anyOf:
                  - true
                oneOf:
                  - type: string
                not:
                  type: "null"
                discriminator:
                  propertyName: kind
                  mapping:
                    user: "#/components/schemas/User"
                  x-ui-label: Type
                unevaluatedProperties: false
                x-ui-order: 10
                """);
        assertEquals(expected, encoded);
        assertSame(
                source.getAdditionalKeywords().get("x-ui-order"),
                assertInstanceOf(ObjectValue.class, encoded)
                        .values()
                        .get("x-ui-order"));
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void keepsTypedKeywordAuthoritativeOnAdditionalKeywordConflict() {
        SchemaDefinition source = definition(JsonType.STRING);
        source.getAdditionalKeywords().put(
                "type",
                new StringValue("integer"));
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("components")
                .child("schemas")
                .child("Conflicting");

        ObjectValue encoded = assertInstanceOf(
                ObjectValue.class,
                encoder.encode(source, context));

        assertEquals(new StringValue("string"), encoded.values().get("type"));
        List<OpenApiDiagnostic> diagnostics = context.diagnostics();
        assertEquals(1, diagnostics.size());
        assertEquals(
                OpenApiDiagnosticCodes.VERSION_FIELD_CONFLICT,
                diagnostics.getFirst().code());
        assertEquals(
                "/components/schemas/Conflicting/type",
                diagnostics.getFirst().path().toPointer());
        assertTrue(context.hasErrors());
    }

    @Test
    void reportsOpenApi32DiscriminatorFieldAsUnsupported() {
        SchemaDefinition source = definition(JsonType.OBJECT);
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("kind");
        discriminator.setDefaultMapping("#/components/schemas/Unknown");
        source.setDiscriminator(discriminator);
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("components")
                .child("schemas")
                .child("Pet");

        ObjectValue encoded = assertInstanceOf(
                ObjectValue.class,
                encoder.encode(source, context));
        ObjectValue encodedDiscriminator = assertInstanceOf(
                ObjectValue.class,
                encoded.values().get("discriminator"));

        assertFalse(encodedDiscriminator.values().containsKey("defaultMapping"));
        assertEquals(
                new StringValue("kind"),
                encodedDiscriminator.values().get("propertyName"));
        List<OpenApiDiagnostic> diagnostics = context.diagnostics();
        assertEquals(1, diagnostics.size());
        assertEquals(
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                diagnostics.getFirst().code());
        assertEquals(
                "/components/schemas/Pet/discriminator/defaultMapping",
                diagnostics.getFirst().path().toPointer());
    }

    private static SchemaDefinition comprehensiveSchema() {
        SchemaDefinition source = definition(
                JsonType.OBJECT,
                JsonType.NULL);
        source.setFormat(new SchemaFormat("aggregate"));
        source.setRef(new UriReference("#/components/schemas/Base"));
        source.setTitle("User");
        source.setDescription("User aggregate");
        source.setDefaultValue(new StringValue("active"));
        source.setExamples(List.of(
                new StringValue("first"),
                new StringValue("second")));
        source.setEnumValues(List.of(
                new StringValue("active"),
                new StringValue("inactive")));
        source.setConstValue(new StringValue("active"));
        source.setDeprecated(true);
        source.setReadOnly(false);
        source.setWriteOnly(true);

        SchemaDefinition id = definition(JsonType.STRING);
        id.setMinLength(BigInteger.ONE);
        id.setMaxLength(BigInteger.valueOf(36));
        id.setPattern("^[a-z]+$");
        source.getProperties().put("id", id);
        source.getRequired().add("id");
        source.setAdditionalProperties(new LogicalSchema(false));
        source.setMinProperties(BigInteger.ONE);
        source.setMaxProperties(BigInteger.valueOf(8));

        source.setItems(new LogicalSchema(true));
        source.setMinItems(BigInteger.ONE);
        source.setMaxItems(BigInteger.valueOf(5));
        source.setUniqueItems(true);
        source.setMinLength(BigInteger.valueOf(2));
        source.setMaxLength(BigInteger.valueOf(64));
        source.setPattern("^[A-Z]");
        source.setMinimum(new BigDecimal("0"));
        source.setMaximum(new BigDecimal("100"));
        source.setExclusiveMinimum(new BigDecimal("-1"));
        source.setExclusiveMaximum(new BigDecimal("101"));
        source.setMultipleOf(new BigDecimal("0.5"));

        SchemaDefinition audited = new SchemaDefinition();
        audited.setRef(new UriReference("#/components/schemas/Audited"));
        source.setAllOf(List.of(audited));
        source.setAnyOf(List.of(new LogicalSchema(true)));
        source.setOneOf(List.of(definition(JsonType.STRING)));
        source.setNotSchema(definition(JsonType.NULL));

        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("kind");
        discriminator.setMapping(Map.of(
                "user",
                "#/components/schemas/User"));
        discriminator.setExtensions(Map.of(
                "x-ui-label",
                new StringValue("Type")));
        source.setDiscriminator(discriminator);
        source.getAdditionalKeywords().put(
                "unevaluatedProperties",
                new BooleanValue(false));
        source.getAdditionalKeywords().put(
                "x-ui-order",
                new NumberValue(new BigDecimal("10")));
        return source;
    }

    private static SchemaDefinition definition(JsonType... types) {
        SchemaDefinition schema = new SchemaDefinition();
        LinkedHashSet<JsonType> orderedTypes = new LinkedHashSet<>();
        orderedTypes.addAll(List.of(types));
        schema.setTypes(orderedTypes);
        return schema;
    }

    private static ObjectValue parseSchema(String content) {
        ObjectValue root = parse("schema:\n" + content.indent(2));
        return assertInstanceOf(
                ObjectValue.class,
                root.values().get("schema"));
    }

    private static ObjectValue parse(String content) {
        JacksonOpenApiSyntaxCodec codec = new JacksonOpenApiSyntaxCodec();
        SyntaxSuccess<?> success = assertInstanceOf(
                SyntaxSuccess.class,
                codec.parse(content, ImportOptions.autoDetect()));
        ParsedDocument parsedDocument = assertInstanceOf(
                ParsedDocument.class,
                success.value());
        return parsedDocument.root();
    }
}
