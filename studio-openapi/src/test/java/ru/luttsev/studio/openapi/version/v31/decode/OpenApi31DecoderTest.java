package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.info.Contact;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.info.License;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.LogicalSchema;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.testing.TestResources;

class OpenApi31DecoderTest {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();

    @Test
    void decodesRootInfoContactAndLicense() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/decode/root-info-contact-license.yaml"));

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertEquals(OpenApiVersion.V3_1_2, document.getOpenApiVersion());
        assertEquals(
                "https://spec.openapis.org/oas/3.1/dialect/base",
                document.getJsonSchemaDialect().value());

        Info info = document.getInfo();
        assertEquals("Example API", info.getTitle());
        assertEquals("Short description", info.getSummary());
        assertEquals("Full description", info.getDescription());
        assertEquals(
                "https://example.com/terms",
                info.getTermsOfService().value());
        assertEquals("1.4.0", info.getVersion());
        assertEquals(
                new StringValue("example"),
                info.getAdditionalFields().get("x-info-id"));

        Contact contact = info.getContact();
        assertEquals("API Team", contact.getName());
        assertEquals(
                "https://example.com/contact",
                contact.getUrl().value());
        assertEquals("api@example.com", contact.getEmail());
        assertEquals(
                new StringValue("team-1"),
                contact.getAdditionalFields().get("x-contact-id"));

        License license = info.getLicense();
        assertEquals("Apache 2.0", license.getName());
        assertEquals("Apache-2.0", license.getIdentifier());
        assertNull(license.getUrl());
        assertEquals(
                new StringValue("public"),
                license.getAdditionalFields().get("x-license-scope"));

        assertTrue(document.getPaths().getItems().isEmpty());
        assertNull(document.getAdditionalFields().get("paths"));
        assertEquals(
                new StringValue("root-value"),
                document.getAdditionalFields().get("x-root-id"));
    }

    @Test
    void reportsMissingRequiredNestedField() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  version: 1.0.0
                paths: {}
                """);

        OpenApiDiagnostic diagnostic = singleFailureDiagnostic(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                diagnostic.code());
        assertEquals("/info/title", diagnostic.path().toPointer());
    }

    @Test
    void decodesComponentSchemasAndResponses() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                components:
                  schemas:
                    Enabled: true
                    User:
                      type: object
                      properties:
                        id:
                          type: string
                  responses:
                    GenericError:
                      description: Error
                """);

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        Components components = document.getComponents();
        assertTrue(assertInstanceOf(
                LogicalSchema.class,
                components.getSchemas().get("Enabled")).isValue());
        SchemaDefinition user = assertInstanceOf(
                SchemaDefinition.class,
                components.getSchemas().get("User"));
        SchemaDefinition id = assertInstanceOf(
                SchemaDefinition.class,
                user.getProperties().get("id"));
        assertEquals(Set.of(JsonType.STRING), id.getTypes());

        InlineObject<?> response = assertInstanceOf(
                InlineObject.class,
                components.getResponses().get("GenericError"));
        assertEquals(
                "Error",
                assertInstanceOf(
                        ApiResponse.class,
                        response.value()).getDescription());
        assertNull(components.getAdditionalFields().get("responses"));
        assertNull(document.getAdditionalFields().get("components"));
    }

    @Test
    void reportsInvalidComponentSchemaAtExactPath() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                components:
                  schemas:
                    Broken: invalid
                """);

        OpenApiDiagnostic diagnostic = singleFailureDiagnostic(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                diagnostic.code());
        assertEquals(
                "/components/schemas/Broken",
                diagnostic.path().toPointer());
    }

    @Test
    void reportsNestedTypeMismatch() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Example API
                  version: 1.0.0
                  contact: invalid
                paths: {}
                """);

        OpenApiDiagnostic diagnostic = singleFailureDiagnostic(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                diagnostic.code());
        assertEquals("/info/contact", diagnostic.path().toPointer());
    }

    @Test
    void reportsMismatchBetweenDetectedAndDeclaredVersion() {
        ObjectValue source = parse("""
                openapi: 3.1.0
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                """);

        OpenApiDiagnostic diagnostic = singleFailureDiagnostic(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        assertEquals(
                OpenApiDiagnosticCodes.MAPPING_VERSION_MISMATCH,
                diagnostic.code());
        assertEquals("/openapi", diagnostic.path().toPointer());
    }

    @Test
    void rejectsUnsupportedVersionFamily() {
        ObjectValue source = parse("""
                openapi: 3.0.4
                info:
                  title: Example API
                  version: 1.0.0
                paths: {}
                """);

        OpenApiDiagnostic diagnostic = singleFailureDiagnostic(
                decoder.decode(source, OpenApiVersion.V3_0_4));

        assertEquals(
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                diagnostic.code());
        assertEquals("/openapi", diagnostic.path().toPointer());
    }

    private static OpenApiDocument successValue(
            AdapterResult<OpenApiDocument> result) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                result);
        assertTrue(success.diagnostics().isEmpty());
        return assertInstanceOf(
                OpenApiDocument.class,
                success.value());
    }

    private static OpenApiDiagnostic singleFailureDiagnostic(
            AdapterResult<OpenApiDocument> result) {
        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                result);
        assertEquals(1, failure.diagnostics().size());
        OpenApiDiagnostic diagnostic = failure.diagnostics().getFirst();
        assertEquals(DiagnosticPhase.MAPPING, diagnostic.phase());
        return diagnostic;
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
