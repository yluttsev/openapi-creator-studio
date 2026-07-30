package ru.luttsev.studio.openapi.result;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public record SyntaxSuccess<T>(
        T value,
        List<OpenApiDiagnostic> diagnostics)
        implements SyntaxResult<T> {

    public SyntaxSuccess {
        Objects.requireNonNull(value, "value must not be null");
        diagnostics = ResultDiagnostics.forSuccess(diagnostics);
    }
}
