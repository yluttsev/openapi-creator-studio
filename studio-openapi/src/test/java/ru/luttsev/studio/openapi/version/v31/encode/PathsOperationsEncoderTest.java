package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.testing.TestResources;
import ru.luttsev.studio.openapi.version.v31.decode.OpenApi31Decoder;

class PathsOperationsEncoderTest {

    @Test
    void roundTripsPathsOperationsAndCallbacks() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/encode/paths-operations-callbacks-round-trip.yaml"));
        OpenApiDocument document = decode(source);
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("paths");

        ObjectValue encoded = new PathsEncoder().encode(
                document.getPaths(),
                context);

        assertEquals(object(source.values().get("paths")), encoded);
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void encodesEveryOpenApi31HttpMethod() {
        PathItem pathItem = new PathItem();
        pathItem.getOperations().put(HttpMethod.GET, new Operation());
        pathItem.getOperations().put(HttpMethod.PUT, new Operation());
        pathItem.getOperations().put(HttpMethod.POST, new Operation());
        pathItem.getOperations().put(HttpMethod.DELETE, new Operation());
        pathItem.getOperations().put(HttpMethod.OPTIONS, new Operation());
        pathItem.getOperations().put(HttpMethod.HEAD, new Operation());
        pathItem.getOperations().put(HttpMethod.PATCH, new Operation());
        pathItem.getOperations().put(HttpMethod.TRACE, new Operation());
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("paths")
                .child("/methods");

        ObjectValue encoded = new PathItemEncoder(
                new PayloadEncoder(new SchemaEncoder()))
                .encode(pathItem, context);

        assertEquals(
                Set.of(
                        "get",
                        "put",
                        "post",
                        "delete",
                        "options",
                        "head",
                        "patch",
                        "trace"),
                encoded.values().keySet());
        for (Object value : encoded.values().values()) {
            ObjectValue operation = object(value);
            assertEquals(Set.of("responses"), operation.values().keySet());
        }
        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void reportsMethodsUnavailableInOpenApi31AtNavigationPaths() {
        PathItem pathItem = new PathItem();
        pathItem.getOperations().put(HttpMethod.QUERY, new Operation());
        pathItem.getOperations().put(new HttpMethod("COPY"), new Operation());
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("paths")
                .child("/search");

        ObjectValue encoded = new PathItemEncoder(
                new PayloadEncoder(new SchemaEncoder()))
                .encode(pathItem, context);

        assertFalse(encoded.values().containsKey("query"));
        assertFalse(encoded.values().containsKey("additionalOperations"));
        assertEquals(2, context.diagnostics().size());
        assertDiagnostic(
                context.diagnostics(),
                "/paths/~1search/query");
        assertDiagnostic(
                context.diagnostics(),
                "/paths/~1search/additionalOperations/COPY");
    }

    @Test
    void roundTripsInheritedAndExplicitlyDisabledSecurity() {
        ObjectValue source = parse("""
                openapi: 3.1.2
                info:
                  title: Security API
                  version: 1.0.0
                paths:
                  /inherits:
                    get:
                      responses: { "200": { description: OK } }
                  /disabled:
                    get:
                      security: []
                      responses: { "200": { description: OK } }
                """);
        OpenApiDocument document = decode(source);
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2)
                .child("paths");

        ObjectValue encoded = new PathsEncoder().encode(
                document.getPaths(),
                context);

        assertEquals(object(source.values().get("paths")), encoded);
        assertTrue(context.diagnostics().isEmpty());
    }

    private static void assertDiagnostic(
            List<OpenApiDiagnostic> diagnostics,
            String path) {
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.code().equals(
                        OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD)
                        && diagnostic.path().toPointer().equals(path)));
    }

    private static ObjectValue object(Object value) {
        return assertInstanceOf(ObjectValue.class, value);
    }

    private static OpenApiDocument decode(ObjectValue source) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                new OpenApi31Decoder().decode(
                        source,
                        OpenApiVersion.V3_1_2));
        assertTrue(success.diagnostics().isEmpty());
        return assertInstanceOf(OpenApiDocument.class, success.value());
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
