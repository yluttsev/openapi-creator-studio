package ru.luttsev.studio.openapi.result;

import java.util.List;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;

public sealed interface AdapterResult<T>
        permits AdapterSuccess, AdapterFailure {

    List<OpenApiDiagnostic> diagnostics();
}
