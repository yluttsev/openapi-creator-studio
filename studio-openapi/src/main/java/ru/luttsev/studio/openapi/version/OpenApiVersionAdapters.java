package ru.luttsev.studio.openapi.version;

import java.util.List;
import ru.luttsev.studio.openapi.version.v31.OpenApi31VersionAdapter;

public final class OpenApiVersionAdapters {

    private OpenApiVersionAdapters() {
    }

    public static OpenApiVersionAdapterRegistry defaults() {
        return new OpenApiVersionAdapterRegistry(
                List.of(new OpenApi31VersionAdapter()));
    }
}
