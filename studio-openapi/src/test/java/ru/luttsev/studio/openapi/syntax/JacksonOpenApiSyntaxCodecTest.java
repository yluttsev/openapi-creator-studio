package ru.luttsev.studio.openapi.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NullValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.SyntaxFailure;
import ru.luttsev.studio.openapi.result.SyntaxResult;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;

class JacksonOpenApiSyntaxCodecTest {

    private final OpenApiSyntaxCodec codec = new JacksonOpenApiSyntaxCodec();

    @Test
    void parsesYamlAndDetectsFormat() {
        String content = """
                openapi: 3.1.0
                info:
                  title: Sample API
                  enabled: true
                  count: 2
                  tags:
                    - public
                    - null
                """;

        ParsedDocument parsedDocument = successValue(
                codec.parse(content, ImportOptions.autoDetect()),
                ParsedDocument.class);

        assertEquals(OpenApiFormat.YAML, parsedDocument.format());
        assertEquals(
                new StringValue("3.1.0"),
                parsedDocument.root().values().get("openapi"));

        ObjectValue info = assertInstanceOf(
                ObjectValue.class,
                parsedDocument.root().values().get("info"));
        assertEquals(
                new StringValue("Sample API"),
                info.values().get("title"));
        assertEquals(
                new BooleanValue(true),
                info.values().get("enabled"));
        assertEquals(
                new NumberValue(new BigDecimal("2")),
                info.values().get("count"));
        assertEquals(
                new ArrayValue(List.of(
                        new StringValue("public"),
                        NullValue.INSTANCE)),
                info.values().get("tags"));
    }

    @Test
    void parsesJsonWithoutLosingNumberPrecision() {
        String content = """
                {
                  "openapi": "3.1.0",
                  "exact": 12345678901234567890.12345678901234567890,
                  "enabled": false,
                  "value": null
                }
                """;

        ParsedDocument parsedDocument = successValue(
                codec.parse(content, ImportOptions.autoDetect()),
                ParsedDocument.class);

        assertEquals(OpenApiFormat.JSON, parsedDocument.format());
        NumberValue exact = assertInstanceOf(
                NumberValue.class,
                parsedDocument.root().values().get("exact"));
        assertEquals(
                new BigDecimal("12345678901234567890.12345678901234567890"),
                exact.value());
        assertEquals(
                new BooleanValue(false),
                parsedDocument.root().values().get("enabled"));
        assertEquals(
                NullValue.INSTANCE,
                parsedDocument.root().values().get("value"));
    }

    @Test
    void explicitFormatOverridesAutomaticDetection() {
        SyntaxResult<ParsedDocument> result = codec.parse(
                "openapi: 3.1.0",
                ImportOptions.forFormat(OpenApiFormat.JSON));

        OpenApiDiagnostic diagnostic = failureDiagnostic(result);

        assertEquals(OpenApiDiagnosticCodes.INVALID_SYNTAX, diagnostic.code());
        assertTrue(diagnostic.message().contains("JSON"));
        assertTrue(diagnostic.sourcePosition().isPresent());
    }

    @Test
    void malformedJsonIsNotReinterpretedAsYaml() {
        SyntaxResult<ParsedDocument> result = codec.parse(
                "{ invalid",
                ImportOptions.autoDetect());

        OpenApiDiagnostic diagnostic = failureDiagnostic(result);

        assertEquals(OpenApiDiagnosticCodes.INVALID_SYNTAX, diagnostic.code());
        assertTrue(diagnostic.message().contains("JSON"));
    }

    @Test
    void rejectsDuplicateObjectProperties() {
        SyntaxResult<ParsedDocument> jsonResult = codec.parse(
                """
                        {
                          "openapi": "3.1.0",
                          "openapi": "3.0.4"
                        }
                        """,
                ImportOptions.forFormat(OpenApiFormat.JSON));
        SyntaxResult<ParsedDocument> yamlResult = codec.parse(
                """
                        openapi: 3.1.0
                        openapi: 3.0.4
                        """,
                ImportOptions.forFormat(OpenApiFormat.YAML));

        assertEquals(
                OpenApiDiagnosticCodes.INVALID_SYNTAX,
                failureDiagnostic(jsonResult).code());
        assertEquals(
                OpenApiDiagnosticCodes.INVALID_SYNTAX,
                failureDiagnostic(yamlResult).code());
    }

    @Test
    void rejectsMultipleYamlDocuments() {
        SyntaxResult<ParsedDocument> result = codec.parse(
                """
                        openapi: 3.1.0
                        ---
                        openapi: 3.1.1
                        """,
                ImportOptions.forFormat(OpenApiFormat.YAML));

        assertEquals(
                OpenApiDiagnosticCodes.INVALID_SYNTAX,
                failureDiagnostic(result).code());
    }

    @Test
    void rejectsEmptyDocumentAndNonObjectRoot() {
        SyntaxResult<ParsedDocument> emptyResult = codec.parse(
                " \n",
                ImportOptions.autoDetect());
        SyntaxResult<ParsedDocument> arrayResult = codec.parse(
                "[]",
                ImportOptions.autoDetect());

        assertEquals(
                OpenApiDiagnosticCodes.EMPTY_DOCUMENT,
                failureDiagnostic(emptyResult).code());
        assertEquals(
                OpenApiDiagnosticCodes.ROOT_NOT_OBJECT,
                failureDiagnostic(arrayResult).code());
    }

    @Test
    void detectsJsonAfterByteOrderMarkAndWhitespace() {
        ParsedDocument parsedDocument = successValue(
                codec.parse(
                        "\uFEFF  {\"openapi\":\"3.1.0\"}",
                        ImportOptions.autoDetect()),
                ParsedDocument.class);

        assertEquals(OpenApiFormat.JSON, parsedDocument.format());
    }

    @Test
    void roundTripsJson() {
        assertRoundTrip(OpenApiFormat.JSON);
    }

    @Test
    void roundTripsYaml() {
        assertRoundTrip(OpenApiFormat.YAML);
    }

    private void assertRoundTrip(OpenApiFormat format) {
        ObjectValue source = sampleDocument();

        String content = successValue(
                codec.write(source, format),
                String.class);
        ParsedDocument parsedDocument = successValue(
                codec.parse(content, ImportOptions.forFormat(format)),
                ParsedDocument.class);

        assertEquals(source, parsedDocument.root());
        assertEquals(format, parsedDocument.format());
        assertFalse(content.isBlank());
        if (format == OpenApiFormat.YAML) {
            assertFalse(content.startsWith("---"));
        }
    }

    private static ObjectValue sampleDocument() {
        LinkedHashMap<String, DocumentValue> info = new LinkedHashMap<>();
        info.put("title", new StringValue("Example API"));
        info.put("active", new BooleanValue(true));
        info.put("score", new NumberValue(new BigDecimal("12.125")));
        info.put("tags", new ArrayValue(List.of(
                new StringValue("123"),
                NullValue.INSTANCE)));

        LinkedHashMap<String, DocumentValue> document = new LinkedHashMap<>();
        document.put("openapi", new StringValue("3.1.0"));
        document.put("info", new ObjectValue(info));
        document.put("paths", new ObjectValue(Map.of()));
        return new ObjectValue(document);
    }

    private static OpenApiDiagnostic failureDiagnostic(
            SyntaxResult<?> result) {
        SyntaxFailure<?> failure = assertInstanceOf(
                SyntaxFailure.class,
                result);
        assertEquals(1, failure.diagnostics().size());
        OpenApiDiagnostic diagnostic = failure.diagnostics().getFirst();
        assertEquals(DiagnosticPhase.PARSING, diagnostic.phase());
        return diagnostic;
    }

    private static <T> T successValue(
            SyntaxResult<?> result,
            Class<T> valueType) {
        SyntaxSuccess<?> success = assertInstanceOf(
                SyntaxSuccess.class,
                result);
        assertTrue(success.diagnostics().isEmpty());
        return valueType.cast(success.value());
    }
}
