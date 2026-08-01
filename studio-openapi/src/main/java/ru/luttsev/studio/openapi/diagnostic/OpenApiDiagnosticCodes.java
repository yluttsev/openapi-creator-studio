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
    public static final DiagnosticCode MISSING_VERSION =
            new DiagnosticCode("openapi.version.missing");
    public static final DiagnosticCode INVALID_VERSION_TYPE =
            new DiagnosticCode("openapi.version.invalid-type");
    public static final DiagnosticCode INVALID_VERSION =
            new DiagnosticCode("openapi.version.invalid");
    public static final DiagnosticCode INVALID_STRUCTURE =
            new DiagnosticCode("openapi.structure.invalid");
    public static final DiagnosticCode UNSUPPORTED_STRUCTURE_VERSION =
            new DiagnosticCode("openapi.structure.unsupported-version");
    public static final DiagnosticCode MAPPING_MISSING_REQUIRED_FIELD =
            new DiagnosticCode("openapi.mapping.missing-required-field");
    public static final DiagnosticCode MAPPING_TYPE_MISMATCH =
            new DiagnosticCode("openapi.mapping.type-mismatch");
    public static final DiagnosticCode MAPPING_INVALID_VALUE =
            new DiagnosticCode("openapi.mapping.invalid-value");
    public static final DiagnosticCode MAPPING_VERSION_MISMATCH =
            new DiagnosticCode("openapi.mapping.version-mismatch");
    public static final DiagnosticCode UNSUPPORTED_MAPPING_VERSION =
            new DiagnosticCode("openapi.mapping.unsupported-version");
    public static final DiagnosticCode VERSION_FIELD_CONFLICT =
            new DiagnosticCode("openapi.compatibility.field-conflict");
    public static final DiagnosticCode VERSION_UNSUPPORTED_FIELD =
            new DiagnosticCode("openapi.compatibility.unsupported-field");
    public static final DiagnosticCode VERSION_UNSUPPORTED_REFERENCE =
            new DiagnosticCode("openapi.compatibility.unsupported-reference");
    public static final DiagnosticCode SERIALIZATION_FAILED =
            new DiagnosticCode("openapi.serialization.failed");

    private OpenApiDiagnosticCodes() {
    }
}
