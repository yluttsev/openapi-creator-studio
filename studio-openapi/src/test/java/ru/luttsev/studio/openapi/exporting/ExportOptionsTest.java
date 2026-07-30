package ru.luttsev.studio.openapi.exporting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

class ExportOptionsTest {

    @Test
    void containsTargetFormatAndVersion() {
        ExportOptions options = new ExportOptions(
                OpenApiFormat.JSON,
                OpenApiVersion.V3_1_2);

        assertEquals(OpenApiFormat.JSON, options.format());
        assertEquals(OpenApiVersion.V3_1_2, options.targetVersion());
    }

    @Test
    void rejectsMissingTargetValues() {
        assertThrows(
                NullPointerException.class,
                () -> new ExportOptions(
                        null,
                        OpenApiVersion.V3_1_2));
        assertThrows(
                NullPointerException.class,
                () -> new ExportOptions(
                        OpenApiFormat.JSON,
                        null));
    }
}
