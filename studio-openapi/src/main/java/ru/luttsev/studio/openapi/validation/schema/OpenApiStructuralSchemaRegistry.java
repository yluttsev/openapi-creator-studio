package ru.luttsev.studio.openapi.validation.schema;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.model.OpenApiVersion;

public final class OpenApiStructuralSchemaRegistry {

    private final List<OpenApiStructuralSchemaProvider> providers;

    public OpenApiStructuralSchemaRegistry(
            List<OpenApiStructuralSchemaProvider> providers) {
        Objects.requireNonNull(providers, "providers must not be null");
        this.providers = List.copyOf(providers);
    }

    public Optional<StructuralSchemaBundle> find(OpenApiVersion version) {
        Objects.requireNonNull(version, "version must not be null");

        OpenApiStructuralSchemaProvider matchingProvider = null;
        for (OpenApiStructuralSchemaProvider provider : providers) {
            if (!provider.supports(version)) {
                continue;
            }
            if (matchingProvider != null) {
                throw new IllegalStateException(
                        "Multiple structural schemas support OpenAPI "
                                + version.value());
            }
            matchingProvider = provider;
        }
        return matchingProvider == null
                ? Optional.empty()
                : Optional.of(matchingProvider.schema());
    }
}
