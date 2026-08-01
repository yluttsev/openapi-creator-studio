package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.LogicalSchema;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.testing.TestResources;

class SchemaDecoderTest {

    private final SchemaDecoder decoder = new SchemaDecoder();

    @Test
    void decodesLogicalSchemas() {
        DecodeContext context = DecodeContext.root(OpenApiVersion.V3_1_2);

        LogicalSchema allowed = assertInstanceOf(
                LogicalSchema.class,
                decoder.decode(new BooleanValue(true), context));
        LogicalSchema denied = assertInstanceOf(
                LogicalSchema.class,
                decoder.decode(new BooleanValue(false), context));

        assertTrue(allowed.isValue());
        assertFalse(denied.isValue());
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void decodesSchemaDefinitionRecursively() {
        ObjectValue source = parseSchema(TestResources.readFixture(
                "v31/decode/schema-definition.fragment.yaml"));
        DecodeContext context = DecodeContext.root(OpenApiVersion.V3_1_2)
                .child("components")
                .child("schemas")
                .child("User");

        SchemaDefinition schema = assertInstanceOf(
                SchemaDefinition.class,
                decoder.decode(source, context));

        assertEquals(Set.of(JsonType.OBJECT, JsonType.NULL), schema.getTypes());
        assertEquals("aggregate", schema.getFormat().value());
        assertEquals("#/components/schemas/Base", schema.getRef().value());
        assertEquals("User", schema.getTitle());
        assertEquals("User aggregate", schema.getDescription());
        assertEquals(new StringValue("active"), schema.getDefaultValue());
        assertEquals(
                List.of(
                        new StringValue("first"),
                        new StringValue("second"),
                        new StringValue("third")),
                schema.getExamples());
        assertEquals(
                List.of(
                        new StringValue("active"),
                        new StringValue("inactive")),
                schema.getEnumValues());
        assertEquals(new StringValue("active"), schema.getConstValue());
        assertEquals(true, schema.getDeprecated());
        assertEquals(false, schema.getReadOnly());
        assertEquals(true, schema.getWriteOnly());
        assertEquals(Set.of("id"), schema.getRequired());
        assertEquals(BigInteger.ONE, schema.getMinProperties());
        assertEquals(BigInteger.valueOf(8), schema.getMaxProperties());

        SchemaDefinition id = assertInstanceOf(
                SchemaDefinition.class,
                schema.getProperties().get("id"));
        assertEquals(Set.of(JsonType.STRING), id.getTypes());
        assertEquals(BigInteger.ONE, id.getMinLength());
        assertEquals(BigInteger.valueOf(36), id.getMaxLength());
        assertEquals("^[a-z]+$", id.getPattern());

        SchemaDefinition score = assertInstanceOf(
                SchemaDefinition.class,
                schema.getProperties().get("score"));
        assertEquals(new BigDecimal("0"), score.getMinimum());
        assertEquals(new BigDecimal("100"), score.getMaximum());
        assertEquals(new BigDecimal("-1"), score.getExclusiveMinimum());
        assertEquals(new BigDecimal("101"), score.getExclusiveMaximum());
        assertEquals(new BigDecimal("0.5"), score.getMultipleOf());

        SchemaDefinition tags = assertInstanceOf(
                SchemaDefinition.class,
                schema.getProperties().get("tags"));
        assertEquals(BigInteger.ONE, tags.getMinItems());
        assertEquals(BigInteger.valueOf(5), tags.getMaxItems());
        assertEquals(true, tags.getUniqueItems());
        SchemaDefinition tagItem = assertInstanceOf(
                SchemaDefinition.class,
                tags.getItems());
        assertEquals(Set.of(JsonType.STRING), tagItem.getTypes());

        LogicalSchema additionalProperties = assertInstanceOf(
                LogicalSchema.class,
                schema.getAdditionalProperties());
        assertFalse(additionalProperties.isValue());
        SchemaDefinition allOf = assertInstanceOf(
                SchemaDefinition.class,
                schema.getAllOf().getFirst());
        assertEquals(
                "#/components/schemas/Audited",
                allOf.getRef().value());
        assertTrue(assertInstanceOf(
                LogicalSchema.class,
                schema.getAnyOf().getFirst()).isValue());
        assertEquals(
                Set.of(JsonType.STRING),
                assertInstanceOf(
                        SchemaDefinition.class,
                        schema.getOneOf().getFirst()).getTypes());
        assertEquals(
                Set.of(JsonType.NULL),
                assertInstanceOf(
                        SchemaDefinition.class,
                        schema.getNotSchema()).getTypes());

        assertEquals("kind", schema.getDiscriminator().getPropertyName());
        assertEquals(
                "#/components/schemas/User",
                schema.getDiscriminator().getMapping().get("user"));
        assertEquals(
                new StringValue("Type"),
                schema.getDiscriminator().getExtensions().get("x-ui-label"));
        assertSame(
                source.values().get("unevaluatedProperties"),
                schema.getAdditionalKeywords().get("unevaluatedProperties"));
        assertSame(
                source.values().get("x-ui-order"),
                schema.getAdditionalKeywords().get("x-ui-order"));
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void reportsInvalidValuesAtExactSchemaPaths() {
        ObjectValue source = parseSchema("""
                type: [string, mystery, 3]
                required: [id, false]
                items: invalid
                minItems: 1.5
                """);
        DecodeContext context = DecodeContext.root(OpenApiVersion.V3_1_2)
                .child("components")
                .child("schemas")
                .child("Broken");

        Schema result = decoder.decode(source, context);

        assertInstanceOf(SchemaDefinition.class, result);
        List<OpenApiDiagnostic> diagnostics = context.diagnostics();
        assertEquals(5, diagnostics.size());
        assertDiagnostic(
                diagnostics.get(0),
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                "/components/schemas/Broken/type/1");
        assertDiagnostic(
                diagnostics.get(1),
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/schemas/Broken/type/2");
        assertDiagnostic(
                diagnostics.get(2),
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/schemas/Broken/required/1");
        assertDiagnostic(
                diagnostics.get(3),
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "/components/schemas/Broken/items");
        assertDiagnostic(
                diagnostics.get(4),
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                "/components/schemas/Broken/minItems");
    }

    private static void assertDiagnostic(
            OpenApiDiagnostic diagnostic,
            DiagnosticCode code,
            String path) {
        assertEquals(code, diagnostic.code());
        assertEquals(path, diagnostic.path().toPointer());
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
