package ru.luttsev.studio.core.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.navigation.DocumentPath;

class ReferenceResolverTest {

    private final ReferenceResolver resolver = new ReferenceResolver();

    @Test
    void resolvesLocalJsonPointer() {
        OpenApiDocument document = createDocument();
        SchemaDefinition userSchema = new SchemaDefinition();
        document.getComponents().getSchemas().put("User", userSchema);
        UriReference reference =
                new UriReference("#/components/schemas/User");

        ReferenceResolution<SchemaDefinition> resolution = resolver.resolve(
                document,
                reference,
                SchemaDefinition.class);

        assertEquals(
                new ResolvedReference<>(
                        userSchema,
                        DocumentPath.parse("/components/schemas/User")),
                resolution);
    }

    @Test
    void resolvesRootReferences() {
        OpenApiDocument document = createDocument();
        UriReference fragmentReference = new UriReference("#");
        UriReference emptyReference = new UriReference("");

        assertEquals(
                new ResolvedReference<>(document, DocumentPath.root()),
                resolver.resolve(
                        document,
                        fragmentReference,
                        OpenApiDocument.class));
        assertEquals(
                new ResolvedReference<>(document, DocumentPath.root()),
                resolver.resolve(
                        document,
                        emptyReference,
                        OpenApiDocument.class));
    }

    @Test
    void returnsExplicitFailureForUnsupportedReferences() {
        OpenApiDocument document = createDocument();
        UriReference external =
                new UriReference("shared.yaml#/components/schemas/User");
        UriReference anchor = new UriReference("#User");
        UriReference invalidPointer =
                new UriReference("#/components/schemas/User~2");

        assertEquals(
                new UnresolvedReference<>(
                        external,
                        ReferenceFailure.EXTERNAL_REFERENCE),
                resolver.resolve(document, external));
        assertEquals(
                new UnresolvedReference<>(
                        anchor,
                        ReferenceFailure.UNSUPPORTED_ANCHOR),
                resolver.resolve(document, anchor));
        assertEquals(
                new UnresolvedReference<>(
                        invalidPointer,
                        ReferenceFailure.INVALID_REFERENCE),
                resolver.resolve(document, invalidPointer));
    }

    @Test
    void reportsMissingTargetAndTypeMismatch() {
        OpenApiDocument document = createDocument();
        SchemaDefinition userSchema = new SchemaDefinition();
        document.getComponents().getSchemas().put("User", userSchema);
        UriReference missing =
                new UriReference("#/components/schemas/Missing");
        UriReference userReference =
                new UriReference("#/components/schemas/User");

        assertEquals(
                new UnresolvedReference<>(
                        missing,
                        ReferenceFailure.TARGET_NOT_FOUND),
                resolver.resolve(document, missing));
        assertEquals(
                new UnresolvedReference<>(
                        userReference,
                        ReferenceFailure.TYPE_MISMATCH),
                resolver.resolve(document, userReference, Operation.class));
    }

    @Test
    void resolvesOnlyOneReferenceHop() {
        OpenApiDocument document = createDocument();
        SchemaDefinition targetSchema = new SchemaDefinition();
        SchemaDefinition referencingSchema = new SchemaDefinition();
        referencingSchema.setRef(
                new UriReference("#/components/schemas/Target"));
        document.getComponents().getSchemas().put("Target", targetSchema);
        document.getComponents().getSchemas().put("Reference", referencingSchema);
        UriReference reference =
                new UriReference("#/components/schemas/Reference");

        ReferenceResolution<SchemaDefinition> resolution = resolver.resolve(
                document,
                reference,
                SchemaDefinition.class);

        assertEquals(
                new ResolvedReference<>(
                        referencingSchema,
                        DocumentPath.parse("/components/schemas/Reference")),
                resolution);
    }

    private static OpenApiDocument createDocument() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Users API",
                "1.0.0"));
    }
}
