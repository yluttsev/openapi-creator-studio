package ru.luttsev.studio.openapi.version.v31.decode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

class DecodeContextTest {

    private static final DiagnosticCode TEST_CODE =
            new DiagnosticCode("openapi.test");

    @Test
    void materializesEscapedAndDeepPathsOnDemand() {
        DecodeContext context = DecodeContext.root(OpenApiVersion.V3_1_2)
                .child("paths")
                .child("/~")
                .child("");
        for (int index = 0; index < 2_000; index++) {
            context = context.child(Integer.toString(index));
        }

        context.error(TEST_CODE, "deep error");

        OpenApiDiagnostic diagnostic = context.diagnostics().getFirst();
        assertEquals(DiagnosticPhase.MAPPING, diagnostic.phase());
        assertEquals(2_003, diagnostic.path().segments().size());
        assertEquals("paths", diagnostic.path().segments().getFirst());
        assertEquals("/~", diagnostic.path().segments().get(1));
        assertEquals("", diagnostic.path().segments().get(2));
    }
}
