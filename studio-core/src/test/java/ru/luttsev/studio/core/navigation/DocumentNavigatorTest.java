package ru.luttsev.studio.core.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.StringValue;

class DocumentNavigatorTest {

    private final DocumentNavigator navigator = new DocumentNavigator();

    @Test
    void findsNestedOpenApiNodesByJsonPointer() {
        DocumentFixture fixture = createDocument();

        Operation operation = navigator.find(
                        fixture.document(),
                        DocumentPath.parse("/paths/~1users~1{id}/get"),
                        Operation.class)
                .orElseThrow();
        UriReference schemaReference = navigator.find(
                        fixture.document(),
                        DocumentPath.parse(
                                "/paths/~1users~1{id}/get/responses/200"
                                        + "/content/application~1json/schema/$ref"),
                        UriReference.class)
                .orElseThrow();
        SchemaDefinition propertySchema = navigator.find(
                        fixture.document(),
                        DocumentPath.parse("/components/schemas/User/properties/name"),
                        SchemaDefinition.class)
                .orElseThrow();

        assertSame(fixture.operation(), operation);
        assertEquals("#/components/schemas/User", schemaReference.value());
        assertSame(fixture.nameSchema(), propertySchema);
    }

    @Test
    void returnsEmptyForMissingPathOrUnexpectedType() {
        DocumentFixture fixture = createDocument();

        assertTrue(navigator.find(
                        fixture.document(),
                        DocumentPath.parse("/components/schemas/Missing"))
                .isEmpty());
        assertTrue(navigator.find(
                        fixture.document(),
                        DocumentPath.parse("/components/schemas/User"),
                        Operation.class)
                .isEmpty());
    }

    @Test
    void exposesAdditionalFieldsAtTheirSemanticPath() {
        DocumentFixture fixture = createDocument();
        fixture.document().getAdditionalFields().put("x-owner", new StringValue("platform"));

        StringValue owner = navigator.find(
                        fixture.document(),
                        DocumentPath.parse("/x-owner"),
                        StringValue.class)
                .orElseThrow();

        assertEquals("platform", owner.value());
    }

    @Test
    void exposesCustomMethodsThroughAdditionalOperations() {
        DocumentFixture fixture = createDocument();
        Operation customOperation = new Operation();
        fixture.document()
                .getPaths()
                .getItems()
                .get("/users/{id}")
                .getOperations()
                .put(new HttpMethod("PROPFIND"), customOperation);

        Operation found = navigator.find(
                        fixture.document(),
                        DocumentPath.parse(
                                "/paths/~1users~1{id}/additionalOperations/PROPFIND"),
                        Operation.class)
                .orElseThrow();

        assertSame(customOperation, found);
    }

    @Test
    void walksDocumentUsingAddressableOpenApiPaths() {
        DocumentFixture fixture = createDocument();

        Set<String> paths = navigator.walk(fixture.document())
                .map(entry -> entry.path().toPointer())
                .collect(Collectors.toSet());

        assertTrue(paths.contains(""));
        assertTrue(paths.contains("/components/schemas/User"));
        assertTrue(paths.contains("/components/schemas/User/properties/name"));
        assertTrue(paths.contains("/paths/~1users~1{id}/get/responses/200"));
        assertTrue(paths.contains(
                "/paths/~1users~1{id}/get/responses/200/content/application~1json/schema/$ref"));
    }

    @Test
    void stopsDescendingWhenMutableModelContainsCycle() {
        OpenApiDocument document = new OpenApiDocument();
        Components components = new Components();
        SchemaDefinition recursiveSchema = new SchemaDefinition();
        recursiveSchema.getProperties().put("self", recursiveSchema);
        components.getSchemas().put("Recursive", recursiveSchema);
        document.setComponents(components);

        List<String> paths = navigator.walk(document)
                .map(entry -> entry.path().toPointer())
                .toList();

        assertTrue(paths.contains("/components/schemas/Recursive/properties/self"));
        assertTrue(paths.size() < 100);
    }

    private static DocumentFixture createDocument() {
        OpenApiDocument document = new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Users API",
                "1.0.0"));

        SchemaDefinition nameSchema = new SchemaDefinition();
        nameSchema.setTypes(Set.of(JsonType.STRING));

        SchemaDefinition userSchema = new SchemaDefinition();
        userSchema.setTypes(Set.of(JsonType.OBJECT));
        userSchema.getProperties().put("name", nameSchema);
        document.getComponents().getSchemas().put("User", userSchema);

        SchemaDefinition responseSchema = new SchemaDefinition();
        responseSchema.setRef(new UriReference("#/components/schemas/User"));

        MediaType mediaType = new MediaType();
        mediaType.setSchema(responseSchema);

        ApiResponse response = new ApiResponse();
        response.setDescription("User found");
        response.getContent().put(
                MediaTypeName.APPLICATION_JSON,
                new InlineObject<>(mediaType));

        Operation operation = new Operation();
        operation.setOperationId("getUser");
        operation.getResponses().put(
                new ResponseKey("200"),
                new InlineObject<>(response));

        PathItem pathItem = new PathItem();
        pathItem.getOperations().put(HttpMethod.GET, operation);
        document.getPaths().getItems().put("/users/{id}", pathItem);

        return new DocumentFixture(document, operation, nameSchema);
    }

    private record DocumentFixture(
            OpenApiDocument document,
            Operation operation,
            SchemaDefinition nameSchema) {
    }
}
