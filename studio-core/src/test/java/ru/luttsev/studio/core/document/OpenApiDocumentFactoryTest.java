package ru.luttsev.studio.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;

class OpenApiDocumentFactoryTest {

    private final OpenApiDocumentFactory factory = new OpenApiDocumentFactory();

    @Test
    void createsInitializedDocument() {
        var parameters = new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Users API",
                "1.0.0");

        var document = factory.create(parameters);

        assertEquals(OpenApiVersion.V3_1_2, document.getOpenApiVersion());
        assertEquals("Users API", document.getInfo().getTitle());
        assertEquals("1.0.0", document.getInfo().getVersion());
        assertNotNull(document.getPaths());
        assertTrue(document.getPaths().getItems().isEmpty());
        assertNotNull(document.getComponents());
        assertTrue(document.getComponents().getSchemas().isEmpty());
    }

    @Test
    void rejectsBlankRequiredValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new NewDocumentParameters(OpenApiVersion.V3_1_2, " ", "1.0.0"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new NewDocumentParameters(OpenApiVersion.V3_1_2, "Users API", ""));
    }
}
