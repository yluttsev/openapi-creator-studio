package ru.luttsev.studio.openapi.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.OpenApiVersion;

public record OpenApiVersionFamily(int major, int minor) {

    public static final OpenApiVersionFamily V3_1 =
            new OpenApiVersionFamily(3, 1);

    private static final Pattern VERSION_PATTERN =
            Pattern.compile(
                    "(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)");

    public OpenApiVersionFamily {
        if (major < 0) {
            throw new IllegalArgumentException("major must not be negative");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("minor must not be negative");
        }
    }

    public boolean supports(OpenApiVersion version) {
        Objects.requireNonNull(version, "version must not be null");

        Matcher matcher = VERSION_PATTERN.matcher(version.value());
        return matcher.matches()
                && Integer.toString(major).equals(matcher.group(1))
                && Integer.toString(minor).equals(matcher.group(2));
    }
}
