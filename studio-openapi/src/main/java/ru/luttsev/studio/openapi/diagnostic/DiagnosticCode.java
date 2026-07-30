package ru.luttsev.studio.openapi.diagnostic;

import java.util.Objects;

public record DiagnosticCode(String value) {

    public DiagnosticCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
