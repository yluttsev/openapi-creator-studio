package ru.luttsev.studio.openapi.validation.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import ru.luttsev.studio.core.model.OpenApiVersion;

public final class OpenApi31StructuralSchemaProvider
        implements OpenApiStructuralSchemaProvider {

    public static final String ROOT_SCHEMA_ID =
            "https://spec.openapis.org/oas/3.1/schema-base/2025-11-23";

    private static final Pattern SUPPORTED_VERSION =
            Pattern.compile("3\\.1\\.\\d+");
    private static final Map<String, String> RESOURCE_PATHS = Map.of(
            ROOT_SCHEMA_ID,
            "/openapi/3.1/2025-11-23/schema-base.json",
            "https://spec.openapis.org/oas/3.1/schema/2025-11-23",
            "/openapi/3.1/2025-11-23/schema.json",
            "https://spec.openapis.org/oas/3.1/dialect/2024-11-10",
            "/openapi/3.1/2025-11-23/dialect.json",
            "https://spec.openapis.org/oas/3.1/meta/2024-11-10",
            "/openapi/3.1/2025-11-23/meta.json");

    private final StructuralSchemaBundle schema = loadSchema();

    @Override
    public boolean supports(OpenApiVersion version) {
        return SUPPORTED_VERSION.matcher(version.value()).matches();
    }

    @Override
    public StructuralSchemaBundle schema() {
        return schema;
    }

    private static StructuralSchemaBundle loadSchema() {
        LinkedHashMap<String, String> resources = new LinkedHashMap<>();
        for (Map.Entry<String, String> resource : RESOURCE_PATHS.entrySet()) {
            resources.put(
                    resource.getKey(),
                    readResource(resource.getValue()));
        }
        return new StructuralSchemaBundle(ROOT_SCHEMA_ID, resources);
    }

    private static String readResource(String path) {
        try (InputStream input =
                OpenApi31StructuralSchemaProvider.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException(
                        "OpenAPI structural schema resource is missing: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read OpenAPI structural schema resource: " + path,
                    exception);
        }
    }
}
