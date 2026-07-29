package ru.luttsev.studio.core.reference.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.reference.ReferenceFailure;

class ReferenceIndexTest {

    private final ReferenceIndexBuilder builder = new ReferenceIndexBuilder();

    @Test
    void indexesResolvedReferencesInBothDirections() {
        OpenApiDocument document = document();
        SchemaDefinition user = new SchemaDefinition();
        SchemaDefinition order = referenceTo("#/components/schemas/User");
        document.getComponents().getSchemas().put("User", user);
        document.getComponents().getSchemas().put("Order", order);

        ReferenceIndex index = builder.build(document);
        DocumentPath userPath = DocumentPath.parse("/components/schemas/User");
        DocumentPath orderPath = DocumentPath.parse("/components/schemas/Order");
        DocumentPath sourcePath = orderPath.child("$ref");

        assertEquals(1, index.references().size());
        assertEquals(1, index.usagesOf(userPath).size());
        assertEquals(sourcePath, index.usagesOf(userPath).getFirst().sourcePath());
        assertEquals(
                userPath,
                index.usagesOf(userPath).getFirst().targetPath());
        assertTrue(index.isReferenced(userPath));
        assertFalse(index.isReferenced(orderPath));
        assertEquals(
                index.findBySource(sourcePath).orElseThrow(),
                index.referencesFrom(orderPath).getFirst());
    }

    @Test
    void recordsUnresolvedAndExternalReferences() {
        OpenApiDocument document = document();
        document.getComponents()
                .getSchemas()
                .put("Missing", referenceTo("#/components/schemas/Unknown"));
        document.getComponents()
                .getSchemas()
                .put("External", referenceTo("shared.yaml#/components/schemas/User"));

        ReferenceIndex index = builder.build(document);

        assertEquals(2, index.unresolvedReferences().size());
        assertTrue(index.unresolvedReferences().stream()
                .anyMatch(reference ->
                        reference.failure() == ReferenceFailure.TARGET_NOT_FOUND));
        assertTrue(index.unresolvedReferences().stream()
                .anyMatch(reference ->
                        reference.failure() == ReferenceFailure.EXTERNAL_REFERENCE));
        assertInstanceOf(
                UnresolvedReferenceUsage.class,
                index.findBySource(DocumentPath.parse(
                                "/components/schemas/Missing/$ref"))
                        .orElseThrow());
    }

    @Test
    void indexesEverySupportedReferenceLocation() {
        OpenApiDocument document = document();
        document.getComponents().getSchemas().put("User", new SchemaDefinition());
        document.getComponents()
                .getSchemas()
                .put("UserAlias", referenceTo("#/components/schemas/User"));

        PathItem sharedPathItem = new PathItem();
        PathItem referencedPathItem = new PathItem();
        referencedPathItem.setRef(new UriReference(
                "#/components/pathItems/SharedUsers"));
        document.getComponents()
                .getPathItems()
                .put("SharedUsers", sharedPathItem);
        document.getPaths()
                .getItems()
                .put("/users", referencedPathItem);

        Parameter parameter = new Parameter();
        ReferenceObject<Parameter> referencedParameter = new ReferenceObject<>();
        referencedParameter.setRef(new UriReference(
                "#/components/parameters/UserId"));
        document.getComponents()
                .getParameters()
                .put("UserId", new InlineObject<>(parameter));
        document.getComponents()
                .getParameters()
                .put("UserIdAlias", referencedParameter);

        ReferenceIndex index = builder.build(document);

        assertEquals(3, index.resolvedReferences().size());
        assertEquals(
                1,
                index.usagesOf(DocumentPath.parse(
                                "/components/pathItems/SharedUsers"))
                        .size());
        assertEquals(
                1,
                index.usagesOf(DocumentPath.parse(
                                "/components/parameters/UserId"))
                        .size());
    }

    @Test
    void ignoresUriFieldsThatAreNotOpenApiReferences() {
        OpenApiDocument document = document();
        document.setSelf(new UriReference("#/components/schemas/User"));
        document.setJsonSchemaDialect(new UriReference("https://json-schema.org/draft/2020-12/schema"));

        ReferenceIndex index = builder.build(document);

        assertTrue(index.references().isEmpty());
    }

    @Test
    void indexesReferenceCyclesWithoutFollowingThem() {
        OpenApiDocument document = document();
        document.getComponents()
                .getSchemas()
                .put("First", referenceTo("#/components/schemas/Second"));
        document.getComponents()
                .getSchemas()
                .put("Second", referenceTo("#/components/schemas/First"));

        ReferenceIndex index = builder.build(document);

        assertEquals(2, index.resolvedReferences().size());
        assertEquals(
                1,
                index.usagesOf(
                                DocumentPath.parse("/components/schemas/First"))
                        .size());
        assertEquals(
                1,
                index.usagesOf(
                                DocumentPath.parse("/components/schemas/Second"))
                        .size());
    }

    @Test
    void exposesImmutableCollections() {
        OpenApiDocument document = document();
        document.getComponents()
                .getSchemas()
                .put("Missing", referenceTo("#/components/schemas/Unknown"));
        ReferenceIndex index = builder.build(document);

        assertThrows(
                UnsupportedOperationException.class,
                () -> index.references().add(index.references().getFirst()));
        assertThrows(
                UnsupportedOperationException.class,
                () -> index.unresolvedReferences().clear());
        assertEquals(
                List.of(),
                index.usagesOf(DocumentPath.parse("/components/schemas/Unknown")));
    }

    private static OpenApiDocument document() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Test API",
                "1.0.0"));
    }

    private static SchemaDefinition referenceTo(String reference) {
        SchemaDefinition schema = new SchemaDefinition();
        schema.setRef(new UriReference(reference));
        return schema;
    }
}
