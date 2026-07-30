package ru.luttsev.studio.openapi.importing;

import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.openapi.format.OpenApiFormat;

public record ImportOptions(Optional<OpenApiFormat> formatHint) {

    public ImportOptions {
        Objects.requireNonNull(formatHint, "formatHint must not be null");
    }

    public static ImportOptions autoDetect() {
        return new ImportOptions(Optional.empty());
    }

    public static ImportOptions forFormat(OpenApiFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        return new ImportOptions(Optional.of(format));
    }
}
