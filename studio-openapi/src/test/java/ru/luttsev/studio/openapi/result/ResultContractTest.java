package ru.luttsev.studio.openapi.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

class ResultContractTest {

    @Test
    void successfulResultsAcceptWarningsAndCopyDiagnostics() {
        ArrayList<OpenApiDiagnostic> source = new ArrayList<>();
        source.add(warning());

        ImportSuccess importResult = new ImportSuccess(
                new OpenApiDocument(),
                source);
        ExportSuccess exportResult = new ExportSuccess(
                "{}",
                source);
        AdapterSuccess<String> adapterResult = new AdapterSuccess<>(
                "mapped",
                source);
        SyntaxSuccess<String> syntaxResult = new SyntaxSuccess<>(
                "parsed",
                source);
        source.clear();

        assertEquals(1, importResult.diagnostics().size());
        assertEquals(1, exportResult.diagnostics().size());
        assertEquals(1, adapterResult.diagnostics().size());
        assertEquals(1, syntaxResult.diagnostics().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> importResult.diagnostics().clear());
    }

    @Test
    void successfulResultsRejectErrors() {
        List<OpenApiDiagnostic> diagnostics = List.of(error());

        assertThrows(
                IllegalArgumentException.class,
                () -> new ImportSuccess(
                        new OpenApiDocument(),
                        diagnostics));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExportSuccess(
                        "{}",
                        diagnostics));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AdapterSuccess<>(
                        "mapped",
                        diagnostics));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SyntaxSuccess<>(
                        "parsed",
                        diagnostics));
    }

    @Test
    void failedResultsRequireAtLeastOneError() {
        List<OpenApiDiagnostic> warnings = List.of(warning());
        List<OpenApiDiagnostic> errors = List.of(error());

        assertThrows(
                IllegalArgumentException.class,
                () -> new ImportFailure(warnings));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExportFailure(warnings));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AdapterFailure<>(warnings));
        assertThrows(
                IllegalArgumentException.class,
                () -> new SyntaxFailure<>(warnings));

        assertEquals(errors, new ImportFailure(errors).diagnostics());
        assertEquals(errors, new ExportFailure(errors).diagnostics());
        assertEquals(errors, new AdapterFailure<>(errors).diagnostics());
        assertEquals(errors, new SyntaxFailure<>(errors).diagnostics());
    }

    @Test
    void exportSuccessRequiresContent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExportSuccess(" ", List.of()));
    }

    private static OpenApiDiagnostic warning() {
        return new OpenApiDiagnostic(
                new DiagnosticCode("openapi.mapping.preserved-field"),
                DiagnosticSeverity.WARNING,
                DiagnosticPhase.MAPPING,
                "Unknown field was preserved",
                DocumentPath.root());
    }

    private static OpenApiDiagnostic error() {
        return new OpenApiDiagnostic(
                new DiagnosticCode("openapi.structure.invalid"),
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.STRUCTURAL_VALIDATION,
                "Document does not match the OpenAPI schema",
                DocumentPath.root());
    }
}
