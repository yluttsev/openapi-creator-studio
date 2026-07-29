package ru.luttsev.studio.core.document;

import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiVersion;

public record NewDocumentParameters(
        OpenApiVersion openApiVersion,
        String title,
        String apiVersion) {

    public NewDocumentParameters {
        Objects.requireNonNull(openApiVersion, "openApiVersion must not be null");
        requireNotBlank(title, "title");
        requireNotBlank(apiVersion, "apiVersion");
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
