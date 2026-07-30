package ru.luttsev.studio.openapi.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.navigation.DocumentPath;

class OpenApiDiagnosticTest {

    @Test
    void createsDiagnosticWithSourcePosition() {
        SourcePosition sourcePosition = new SourcePosition(12, 7);

        OpenApiDiagnostic diagnostic = new OpenApiDiagnostic(
                new DiagnosticCode("openapi.syntax.invalid"),
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.PARSING,
                "Invalid YAML",
                DocumentPath.root(),
                Optional.of(sourcePosition));

        assertEquals(sourcePosition, diagnostic.sourcePosition().orElseThrow());
    }

    @Test
    void rejectsInvalidDiagnosticValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DiagnosticCode(" "));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SourcePosition(0, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SourcePosition(1, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OpenApiDiagnostic(
                        new DiagnosticCode("openapi.syntax.invalid"),
                        DiagnosticSeverity.ERROR,
                        DiagnosticPhase.PARSING,
                        " ",
                        DocumentPath.root()));
    }
}
