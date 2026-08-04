package ru.luttsev.studio.application.document;

import java.util.List;
import lombok.Getter;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

@Getter
public final class OpenApiProcessingException extends RuntimeException {

    private final List<OpenApiDiagnostic> diagnostics;

    public OpenApiProcessingException(
            String message,
            List<OpenApiDiagnostic> diagnostics) {
        super(message);
        this.diagnostics = List.copyOf(diagnostics);
    }
}
