package ru.luttsev.studio.openapi.version;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;

class OpenApiVersionAdapterRegistryTest {

    @Test
    void findsAdapterForSupportedVersion() {
        TestVersionAdapter adapter =
                new TestVersionAdapter(OpenApiVersion.V3_1_2);
        OpenApiVersionAdapterRegistry registry =
                new OpenApiVersionAdapterRegistry(List.of(adapter));

        assertSame(
                adapter,
                registry.find(OpenApiVersion.V3_1_2).orElseThrow());
        assertTrue(registry.find(OpenApiVersion.V3_0_4).isEmpty());
    }

    @Test
    void copiesAdapterCollection() {
        TestVersionAdapter adapter =
                new TestVersionAdapter(OpenApiVersion.V3_1_2);
        ArrayList<OpenApiVersionAdapter> adapters = new ArrayList<>();
        adapters.add(adapter);
        OpenApiVersionAdapterRegistry registry =
                new OpenApiVersionAdapterRegistry(adapters);
        adapters.clear();

        assertSame(
                adapter,
                registry.find(OpenApiVersion.V3_1_2).orElseThrow());
    }

    @Test
    void rejectsAmbiguousAdapterSelection() {
        OpenApiVersionAdapterRegistry registry =
                new OpenApiVersionAdapterRegistry(List.of(
                        new TestVersionAdapter(OpenApiVersion.V3_1_2),
                        new TestVersionAdapter(OpenApiVersion.V3_1_2)));

        assertThrows(
                IllegalStateException.class,
                () -> registry.find(OpenApiVersion.V3_1_2));
    }
}
