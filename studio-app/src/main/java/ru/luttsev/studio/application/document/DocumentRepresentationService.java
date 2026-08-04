package ru.luttsev.studio.application.document;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterResult;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapter;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapterRegistry;

@Service
@RequiredArgsConstructor
public final class DocumentRepresentationService {

    private final OpenApiVersionAdapterRegistry adapterRegistry;
    private final DocumentValueConverter valueConverter;

    public Map<String, Object> represent(OpenApiDocument document) {
        OpenApiVersionAdapter adapter = adapterRegistry
                .find(document.getOpenApiVersion())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported OpenAPI version: "
                                + document.getOpenApiVersion().value()));
        AdapterResult<ObjectValue> result = adapter.encode(
                document,
                document.getOpenApiVersion());
        if (result instanceof AdapterFailure<ObjectValue> failure) {
            throw new OpenApiProcessingException(
                    "OpenAPI document representation failed",
                    failure.diagnostics());
        }
        AdapterSuccess<ObjectValue> success =
                (AdapterSuccess<ObjectValue>) result;
        return valueConverter.toMap(success.value());
    }
}
