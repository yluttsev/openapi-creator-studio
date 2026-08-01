package ru.luttsev.studio.openapi.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class TestResources {

    private static final String FIXTURE_ROOT =
            "/ru/luttsev/studio/openapi/fixtures/";

    private TestResources() {
    }

    public static String readUtf8(String absoluteClasspathPath) {
        Objects.requireNonNull(
                absoluteClasspathPath,
                "absoluteClasspathPath must not be null");
        if (!absoluteClasspathPath.startsWith("/")) {
            throw new IllegalArgumentException(
                    "Classpath resource path must be absolute: "
                            + absoluteClasspathPath);
        }

        try (InputStream input = TestResources.class.getResourceAsStream(
                absoluteClasspathPath)) {
            if (input == null) {
                throw new IllegalArgumentException(
                        "Classpath resource is missing: "
                                + absoluteClasspathPath);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to read classpath resource: "
                            + absoluteClasspathPath,
                    exception);
        }
    }

    public static String readFixture(String relativePath) {
        Objects.requireNonNull(relativePath, "relativePath must not be null");
        if (relativePath.startsWith("/") || relativePath.contains("..")) {
            throw new IllegalArgumentException(
                    "Fixture path must be relative and stay under fixture root: "
                            + relativePath);
        }
        return readUtf8(FIXTURE_ROOT + relativePath);
    }
}
