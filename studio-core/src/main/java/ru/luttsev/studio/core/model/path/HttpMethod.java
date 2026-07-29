package ru.luttsev.studio.core.model.path;

import java.util.Objects;

public record HttpMethod(String value) {

    public static final HttpMethod GET = new HttpMethod("GET");
    public static final HttpMethod PUT = new HttpMethod("PUT");
    public static final HttpMethod POST = new HttpMethod("POST");
    public static final HttpMethod DELETE = new HttpMethod("DELETE");
    public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");
    public static final HttpMethod HEAD = new HttpMethod("HEAD");
    public static final HttpMethod PATCH = new HttpMethod("PATCH");
    public static final HttpMethod TRACE = new HttpMethod("TRACE");
    public static final HttpMethod QUERY = new HttpMethod("QUERY");

    public HttpMethod {
        Objects.requireNonNull(value, "value must not be null");
    }
}
