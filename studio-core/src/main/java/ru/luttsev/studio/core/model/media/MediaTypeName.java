package ru.luttsev.studio.core.model.media;

import java.util.Objects;

public record MediaTypeName(String value) {

    public static final MediaTypeName APPLICATION_JSON = new MediaTypeName("application/json");
    public static final MediaTypeName APPLICATION_XML = new MediaTypeName("application/xml");
    public static final MediaTypeName MULTIPART_FORM_DATA = new MediaTypeName("multipart/form-data");
    public static final MediaTypeName TEXT_PLAIN = new MediaTypeName("text/plain");

    public MediaTypeName {
        Objects.requireNonNull(value, "value must not be null");
    }
}
