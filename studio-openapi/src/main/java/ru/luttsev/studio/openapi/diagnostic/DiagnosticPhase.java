package ru.luttsev.studio.openapi.diagnostic;

public enum DiagnosticPhase {
    PARSING,
    VERSION_DETECTION,
    STRUCTURAL_VALIDATION,
    MAPPING,
    SEMANTIC_VALIDATION,
    VERSION_COMPATIBILITY,
    SERIALIZATION
}
