package ru.luttsev.studio.openapi.validation.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;

class OpenApiStructuralSchemaRegistryTest {

    @Test
    void findsSchemaForSupportedVersion() {
        StructuralSchemaBundle schema = testSchema("urn:test:openapi-3.1");
        TestStructuralSchemaProvider provider =
                new TestStructuralSchemaProvider(
                        OpenApiVersion.V3_1_2,
                        schema);
        OpenApiStructuralSchemaRegistry registry =
                new OpenApiStructuralSchemaRegistry(List.of(provider));

        assertSame(
                schema,
                registry.find(OpenApiVersion.V3_1_2).orElseThrow());
        assertTrue(registry.find(OpenApiVersion.V3_0_4).isEmpty());
    }

    @Test
    void copiesProviderCollection() {
        StructuralSchemaBundle schema = testSchema("urn:test:openapi-3.1");
        ArrayList<OpenApiStructuralSchemaProvider> providers =
                new ArrayList<>();
        providers.add(new TestStructuralSchemaProvider(
                OpenApiVersion.V3_1_2,
                schema));
        OpenApiStructuralSchemaRegistry registry =
                new OpenApiStructuralSchemaRegistry(providers);
        providers.clear();

        assertSame(
                schema,
                registry.find(OpenApiVersion.V3_1_2).orElseThrow());
    }

    @Test
    void rejectsAmbiguousSchemaSelection() {
        StructuralSchemaBundle first = testSchema("urn:test:first");
        StructuralSchemaBundle second = testSchema("urn:test:second");
        OpenApiStructuralSchemaRegistry registry =
                new OpenApiStructuralSchemaRegistry(List.of(
                        new TestStructuralSchemaProvider(
                                OpenApiVersion.V3_1_2,
                                first),
                        new TestStructuralSchemaProvider(
                                OpenApiVersion.V3_1_2,
                                second)));

        assertThrows(
                IllegalStateException.class,
                () -> registry.find(OpenApiVersion.V3_1_2));
    }

    @Test
    void loadsPinnedOpenApi31SchemaBundle() {
        OpenApi31StructuralSchemaProvider provider =
                new OpenApi31StructuralSchemaProvider();
        StructuralSchemaBundle schema = provider.schema();

        assertTrue(provider.supports(new OpenApiVersion("3.1.0")));
        assertTrue(provider.supports(OpenApiVersion.V3_1_2));
        assertFalse(provider.supports(OpenApiVersion.V3_0_4));
        assertFalse(provider.supports(new OpenApiVersion("3.1.invalid")));
        assertEquals(
                OpenApi31StructuralSchemaProvider.ROOT_SCHEMA_ID,
                schema.rootSchemaId());
        assertEquals(4, schema.resources().size());
        assertTrue(schema.resources().containsKey(schema.rootSchemaId()));
    }

    private static StructuralSchemaBundle testSchema(String id) {
        return new StructuralSchemaBundle(id, Map.of(id, "{}"));
    }
}
