package ru.luttsev.studio.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ClasspathResources {

    private ClasspathResources() {
    }

    public static String readString(String classpathPath) {
        try (InputStream input =
                ClasspathResources.class.getResourceAsStream(classpathPath)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Test resource not found: " + classpathPath);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to read test resource: " + classpathPath,
                    exception);
        }
    }
}
