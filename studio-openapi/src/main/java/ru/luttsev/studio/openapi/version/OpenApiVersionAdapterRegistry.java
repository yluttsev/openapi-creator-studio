package ru.luttsev.studio.openapi.version;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.model.OpenApiVersion;

public final class OpenApiVersionAdapterRegistry {

    private final List<OpenApiVersionAdapter> adapters;

    public OpenApiVersionAdapterRegistry(
            List<OpenApiVersionAdapter> adapters) {
        Objects.requireNonNull(adapters, "adapters must not be null");
        this.adapters = List.copyOf(adapters);
    }

    public Optional<OpenApiVersionAdapter> find(
            OpenApiVersion version) {
        Objects.requireNonNull(version, "version must not be null");

        OpenApiVersionAdapter matchingAdapter = null;
        for (OpenApiVersionAdapter adapter : adapters) {
            if (!adapter.supports(version)) {
                continue;
            }
            if (matchingAdapter != null) {
                throw new IllegalStateException(
                        "Multiple OpenAPI version adapters support "
                                + version.value());
            }
            matchingAdapter = adapter;
        }
        return Optional.ofNullable(matchingAdapter);
    }
}
