package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

class EncodeContextTest {

    private static final DiagnosticCode TEST_CODE =
            new DiagnosticCode("openapi.test");

    @Test
    void sharesDiagnosticsAndTracksExactPaths() {
        EncodeContext context = EncodeContext.root(OpenApiVersion.V3_1_2);

        context.child("paths")
                .child("/users")
                .warning(TEST_CODE, "warning");

        assertFalse(context.hasErrors());
        assertEquals(OpenApiVersion.V3_1_2, context.targetVersion());
        assertEquals("", context.path().toPointer());

        context.child("info")
                .child("title")
                .error(TEST_CODE, "error");

        List<OpenApiDiagnostic> diagnostics = context.diagnostics();
        assertTrue(context.hasErrors());
        assertEquals(2, diagnostics.size());
        assertEquals(DiagnosticSeverity.WARNING, diagnostics.get(0).severity());
        assertEquals(
                DiagnosticPhase.VERSION_COMPATIBILITY,
                diagnostics.get(0).phase());
        assertEquals("/paths/~1users", diagnostics.get(0).path().toPointer());
        assertEquals(DiagnosticSeverity.ERROR, diagnostics.get(1).severity());
        assertEquals("/info/title", diagnostics.get(1).path().toPointer());
    }
}
