package ru.luttsev.studio.openapi.version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;

class OpenApiVersionFamilyTest {

    @Test
    void supportsEveryValidPatchVersionInFamily() {
        assertTrue(OpenApiVersionFamily.V3_1.supports(
                new OpenApiVersion("3.1.0")));
        assertTrue(OpenApiVersionFamily.V3_1.supports(
                new OpenApiVersion("3.1.2")));
        assertTrue(OpenApiVersionFamily.V3_1.supports(
                new OpenApiVersion("3.1.12345678901234567890")));
    }

    @Test
    void rejectsOtherFamiliesAndMalformedVersions() {
        assertFalse(OpenApiVersionFamily.V3_1.supports(
                OpenApiVersion.V3_0_4));
        assertFalse(OpenApiVersionFamily.V3_1.supports(
                OpenApiVersion.V3_2_0));
        assertFalse(OpenApiVersionFamily.V3_1.supports(
                new OpenApiVersion("3.1")));
        assertFalse(OpenApiVersionFamily.V3_1.supports(
                new OpenApiVersion("03.1.2")));
        assertFalse(OpenApiVersionFamily.V3_1.supports(
                new OpenApiVersion("3.1.invalid")));
    }
}
