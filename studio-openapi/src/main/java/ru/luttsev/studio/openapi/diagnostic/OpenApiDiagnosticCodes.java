package ru.luttsev.studio.openapi.diagnostic;

public final class OpenApiDiagnosticCodes {

    public static final DiagnosticCode INVALID_SYNTAX =
            new DiagnosticCode("openapi.syntax.invalid");
    public static final DiagnosticCode EMPTY_DOCUMENT =
            new DiagnosticCode("openapi.syntax.empty-document");
    public static final DiagnosticCode ROOT_NOT_OBJECT =
            new DiagnosticCode("openapi.syntax.root-not-object");
    public static final DiagnosticCode UNSUPPORTED_SYNTAX_VALUE =
            new DiagnosticCode("openapi.syntax.unsupported-value");
    public static final DiagnosticCode SERIALIZATION_FAILED =
            new DiagnosticCode("openapi.serialization.failed");

    private OpenApiDiagnosticCodes() {
    }
}
