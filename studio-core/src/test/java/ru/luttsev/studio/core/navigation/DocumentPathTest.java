package ru.luttsev.studio.core.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentPathTest {

    @Test
    void representsRoot() {
        DocumentPath root = DocumentPath.root();

        assertTrue(root.isRoot());
        assertEquals("", root.toPointer());
        assertEquals("#", root.toFragment());
        assertTrue(root.parent().isEmpty());
        assertEquals(root, DocumentPath.parse(""));
        assertEquals(root, DocumentPath.parseFragment("#"));
    }

    @Test
    void parsesAndEncodesEscapedSegments() {
        DocumentPath path = DocumentPath.parse("/paths/~1users~1{id}/properties/a~0b");

        assertEquals(List.of("paths", "/users/{id}", "properties", "a~b"), path.segments());
        assertEquals("/paths/~1users~1{id}/properties/a~0b", path.toPointer());
    }

    @Test
    void createsChildAndParentPaths() {
        DocumentPath path = DocumentPath.root()
                .child("components")
                .child("schemas")
                .child("User");

        assertFalse(path.isRoot());
        assertEquals("/components/schemas/User", path.toPointer());
        assertEquals(
                "/components/schemas",
                path.parent().orElseThrow().toPointer());
    }

    @Test
    void checksPathPrefixes() {
        DocumentPath schemas = DocumentPath.parse("/components/schemas");
        DocumentPath user = DocumentPath.parse("/components/schemas/User");

        assertTrue(user.startsWith(schemas));
        assertTrue(user.startsWith(DocumentPath.root()));
        assertTrue(user.startsWith(user));
        assertFalse(schemas.startsWith(user));
    }

    @Test
    void replacesPathPrefixAndPreservesRemainingSegments() {
        DocumentPath property =
                DocumentPath.parse("/components/schemas/User/properties/id");

        DocumentPath renamed = property.replacePrefix(
                DocumentPath.parse("/components/schemas/User"),
                DocumentPath.parse("/components/schemas/Customer"));

        assertEquals(
                "/components/schemas/Customer/properties/id",
                renamed.toPointer());
        assertThrows(
                IllegalArgumentException.class,
                () -> property.replacePrefix(
                        DocumentPath.parse("/paths"),
                        DocumentPath.parse("/webhooks")));
    }

    @Test
    void convertsUriFragmentRepresentation() {
        DocumentPath path = DocumentPath.root()
                .child("components")
                .child("schemas")
                .child("User name");

        assertEquals("#/components/schemas/User%20name", path.toFragment());
        assertEquals(path, DocumentPath.parseFragment(path.toFragment()));
    }

    @Test
    void rejectsInvalidPointersAndEscapes() {
        assertThrows(IllegalArgumentException.class, () -> DocumentPath.parse("components/schemas"));
        assertThrows(IllegalArgumentException.class, () -> DocumentPath.parse("/schemas/a~"));
        assertThrows(IllegalArgumentException.class, () -> DocumentPath.parse("/schemas/a~2b"));
        assertThrows(IllegalArgumentException.class, () -> DocumentPath.parseFragment("/schemas/User"));
    }
}
