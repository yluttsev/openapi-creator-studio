package ru.luttsev.studio.openapi.importing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

class ImportOptionsTest {

    @Test
    void supportsAutomaticAndExplicitFormatSelection() {
        ImportOptions automatic = ImportOptions.autoDetect();
        ImportOptions explicit = ImportOptions.forFormat(OpenApiFormat.YAML);

        assertTrue(automatic.formatHint().isEmpty());
        assertEquals(
                OpenApiFormat.YAML,
                explicit.formatHint().orElseThrow());
    }

    @Test
    void rejectsNullExplicitFormat() {
        assertThrows(
                NullPointerException.class,
                () -> ImportOptions.forFormat(null));
    }
}
