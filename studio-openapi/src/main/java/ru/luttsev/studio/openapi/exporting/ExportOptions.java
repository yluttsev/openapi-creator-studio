package ru.luttsev.studio.openapi.exporting;

import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

public record ExportOptions(
        OpenApiFormat format,
        OpenApiVersion targetVersion) {

    public ExportOptions {
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(targetVersion, "targetVersion must not be null");
    }
}
