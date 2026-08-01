package ru.luttsev.studio.openapi.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestResourcesTest {

    @Test
    void readsUtf8Content() {
        assertEquals(
                "Привет — café\n",
                TestResources.readFixture("testing/utf8.txt"));
    }

    @Test
    void rejectsRelativeAndMissingPaths() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TestResources.readUtf8("fixture.yaml"));
        assertThrows(
                IllegalArgumentException.class,
                () -> TestResources.readFixture("../outside.yaml"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TestResources.readUtf8(
                        "/ru/luttsev/studio/openapi/fixtures/missing.yaml"));
        assertTrue(exception.getMessage().contains("missing.yaml"));
    }
}
