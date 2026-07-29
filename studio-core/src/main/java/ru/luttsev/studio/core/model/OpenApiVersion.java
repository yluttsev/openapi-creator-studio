package ru.luttsev.studio.core.model;

import java.util.Objects;

public record OpenApiVersion(String value) {

    public static final OpenApiVersion V3_0_4 = new OpenApiVersion("3.0.4");
    public static final OpenApiVersion V3_1_2 = new OpenApiVersion("3.1.2");
    public static final OpenApiVersion V3_2_0 = new OpenApiVersion("3.2.0");

    public OpenApiVersion {
        Objects.requireNonNull(value, "value must not be null");
    }
}
