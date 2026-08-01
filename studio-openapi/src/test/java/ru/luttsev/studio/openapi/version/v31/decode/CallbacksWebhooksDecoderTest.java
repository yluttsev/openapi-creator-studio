package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.testing.TestResources;

class CallbacksWebhooksDecoderTest {

    private final OpenApi31Decoder decoder = new OpenApi31Decoder();

    @Test
    void decodesOperationCallbacksRootWebhooksAndComponentPathItems() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/decode/callbacks-webhooks-path-items.yaml"));

        OpenApiDocument document = successValue(
                decoder.decode(source, OpenApiVersion.V3_1_2));

        Operation operation = document.getPaths()
                .getItems()
                .get("/orders")
                .getOperations()
                .get(HttpMethod.POST);
        Callback callback = inlineValue(
                operation.getCallbacks().get("orderStatus"),
                Callback.class);
        PathItem callbackPathItem = callback.getExpressions()
                .get("{$request.body#/callbackUrl}");
        assertEquals(
                "reportOrderStatus",
                callbackPathItem.getOperations()
                        .get(HttpMethod.POST)
                        .getOperationId());
        assertEquals(
                new StringValue("orders-team"),
                callback.getAdditionalFields().get("x-callback-owner"));
        assertInstanceOf(
                ReferenceObject.class,
                operation.getCallbacks().get("shared"));
        assertNull(operation.getAdditionalFields().get("callbacks"));

        assertEquals(
                "orderCreated",
                document.getWebhooks()
                        .get("orderCreated")
                        .getOperations()
                        .get(HttpMethod.POST)
                        .getOperationId());
        assertEquals(
                "#/components/pathItems/SharedWebhook",
                document.getWebhooks()
                        .get("referencedWebhook")
                        .getRef()
                        .value());
        assertNull(document.getAdditionalFields().get("webhooks"));

        Callback shared = inlineValue(
                document.getComponents().getCallbacks().get("Shared"),
                Callback.class);
        assertTrue(shared.getExpressions().containsKey(
                "{$request.query.callbackUrl}"));
        assertInstanceOf(
                ReferenceObject.class,
                document.getComponents().getCallbacks().get("SharedAlias"));
        assertEquals(
                "Shared webhook",
                document.getComponents()
                        .getPathItems()
                        .get("SharedWebhook")
                        .getSummary());
        assertFalse(document.getComponents()
                .getAdditionalFields()
                .containsKey("callbacks"));
        assertFalse(document.getComponents()
                .getAdditionalFields()
                .containsKey("pathItems"));
    }

    @Test
    void reportsCallbackAndWebhookMappingErrorsAtExactPaths() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/decode/invalid/callbacks-webhooks-errors.yaml"));

        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                decoder.decode(source, OpenApiVersion.V3_1_2));
        Set<String> diagnosticPaths = failure.diagnostics().stream()
                .map(OpenApiDiagnostic::path)
                .map(path -> path.toPointer())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "/paths/~1orders/post/callbacks/invalidReferenceOr",
                "/paths/~1orders/post/callbacks/invalidExpression/not-an-expression",
                "/webhooks/broken",
                "/components/callbacks/Broken",
                "/components/callbacks/BrokenExpression/not-an-expression",
                "/components/pathItems/Broken"), diagnosticPaths);
    }

    private static <T> T inlineValue(Object source, Class<T> valueType) {
        InlineObject<?> inline = assertInstanceOf(InlineObject.class, source);
        return assertInstanceOf(valueType, inline.value());
    }

    private static OpenApiDocument successValue(
            AdapterResult<OpenApiDocument> result) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                result);
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
