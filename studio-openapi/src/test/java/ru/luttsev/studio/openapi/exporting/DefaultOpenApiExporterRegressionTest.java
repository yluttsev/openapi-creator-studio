package ru.luttsev.studio.openapi.exporting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.openapi.result.ExportFailure;

class DefaultOpenApiExporterRegressionTest {

    private static final ExportOptions YAML_31 = new ExportOptions(
            OpenApiFormat.YAML,
            OpenApiVersion.V3_1_2);

    private final OpenApiExporter exporter = new DefaultOpenApiExporter();

    @Test
    void reportsNullCollectionElementInsteadOfThrowing() {
        OpenApiDocument document = validDocument();
        Server server = new Server();
        server.setUrl("https://example.com");
        document.setServers(Arrays.asList(server, null));

        ExportFailure failure = assertInstanceOf(
                ExportFailure.class,
                exporter.exportDocument(document, YAML_31));

        assertDiagnostic(
                failure.diagnostics().getFirst(),
                OpenApiDiagnosticCodes.MAPPING_INVALID_VALUE,
                "/servers/1");
    }

    @Test
    void reportsCyclicInlineCallbackInsteadOfOverflowingStack() {
        OpenApiDocument document = validDocument();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        ApiResponse response = new ApiResponse();
        response.setDescription("Accepted");
        operation.getResponses().getValues().put(
                new ResponseKey("202"),
                new InlineObject<>(response));
        pathItem.getOperations().put(HttpMethod.POST, operation);
        document.getPaths().getItems().put("/events", pathItem);

        Callback callback = new Callback();
        callback.getExpressions().put(
                "{$request.body#/callbackUrl}",
                pathItem);
        operation.getCallbacks().put(
                "onEvent",
                new InlineObject<>(callback));

        ExportFailure failure = assertInstanceOf(
                ExportFailure.class,
                exporter.exportDocument(document, YAML_31));

        assertDiagnostic(
                failure.diagnostics().getFirst(),
                OpenApiDiagnosticCodes.MAPPING_CYCLIC_INLINE_OBJECT,
                "/paths/~1events/post/callbacks/onEvent/"
                        + "{$request.body#~1callbackUrl}");
    }

    @Test
    void reportsCyclicInlineSchemaInsteadOfOverflowingStack() {
        OpenApiDocument document = validDocument();
        SchemaDefinition schema = new SchemaDefinition();
        schema.getTypes().add(JsonType.OBJECT);
        schema.getProperties().put("self", schema);
        document.getComponents().getSchemas().put("Recursive", schema);

        ExportFailure failure = assertInstanceOf(
                ExportFailure.class,
                exporter.exportDocument(document, YAML_31));

        assertDiagnostic(
                failure.diagnostics().getFirst(),
                OpenApiDiagnosticCodes.MAPPING_CYCLIC_INLINE_OBJECT,
                "/components/schemas/Recursive/properties/self");
    }

    private static OpenApiDocument validDocument() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Regression API",
                "1.0.0"));
    }

    private static void assertDiagnostic(
            OpenApiDiagnostic diagnostic,
            DiagnosticCode code,
            String path) {
        assertEquals(code, diagnostic.code());
        assertEquals(DiagnosticPhase.MAPPING, diagnostic.phase());
        assertEquals(path, diagnostic.path().toPointer());
    }
}
