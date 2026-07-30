package ru.luttsev.studio.openapi.validation;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.internal.jackson.JacksonDocumentValueMapper;
import ru.luttsev.studio.openapi.result.StructuralValidationFailure;
import ru.luttsev.studio.openapi.result.StructuralValidationResult;
import ru.luttsev.studio.openapi.result.StructuralValidationSuccess;
import ru.luttsev.studio.openapi.validation.schema.OpenApi31StructuralSchemaProvider;
import ru.luttsev.studio.openapi.validation.schema.OpenApiStructuralSchemaRegistry;
import ru.luttsev.studio.openapi.validation.schema.StructuralSchemaBundle;
import tools.jackson.databind.JsonNode;

public final class DefaultOpenApiStructuralValidator
        implements OpenApiStructuralValidator {

    private static final DocumentPath VERSION_PATH =
            DocumentPath.root().child("openapi");

    private final OpenApiStructuralSchemaRegistry schemaRegistry;
    private final JacksonDocumentValueMapper valueMapper;
    private final ConcurrentMap<String, Schema> compiledSchemas;

    public DefaultOpenApiStructuralValidator() {
        this(new OpenApiStructuralSchemaRegistry(
                List.of(new OpenApi31StructuralSchemaProvider())));
    }

    public DefaultOpenApiStructuralValidator(
            OpenApiStructuralSchemaRegistry schemaRegistry) {
        this.schemaRegistry = Objects.requireNonNull(
                schemaRegistry,
                "schemaRegistry must not be null");
        valueMapper = new JacksonDocumentValueMapper();
        compiledSchemas = new ConcurrentHashMap<>();
    }

    @Override
    public StructuralValidationResult validate(
            ObjectValue document,
            OpenApiVersion version) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(version, "version must not be null");

        Optional<StructuralSchemaBundle> schema = schemaRegistry.find(version);
        if (schema.isEmpty()) {
            return unsupportedVersion(version);
        }

        StructuralSchemaBundle schemaBundle = schema.orElseThrow();
        Schema compiledSchema = compiledSchemas.computeIfAbsent(
                schemaBundle.rootSchemaId(),
                ignored -> compile(schemaBundle));
        JsonNode documentNode = valueMapper.toJsonNode(document);
        List<Error> errors = compiledSchema.validate(
                documentNode,
                executionContext -> executionContext.executionConfig(
                        config -> config.formatAssertionsEnabled(true)));
        if (errors.isEmpty()) {
            return new StructuralValidationSuccess(List.of());
        }

        ArrayList<OpenApiDiagnostic> diagnostics =
                new ArrayList<>(errors.size());
        for (Error error : errors) {
            diagnostics.add(toDiagnostic(error));
        }
        diagnostics.sort(Comparator
                .comparing((OpenApiDiagnostic diagnostic) ->
                        diagnostic.path().toPointer())
                .thenComparing(OpenApiDiagnostic::message));
        return new StructuralValidationFailure(diagnostics);
    }

    private static Schema compile(StructuralSchemaBundle schemaBundle) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
                SpecificationVersion.DRAFT_2020_12,
                builder -> builder
                        .schemas(schemaBundle.resources())
                        .schemaRegistryConfig(SchemaRegistryConfig.builder()
                                .formatAssertionsEnabled(true)
                                .build()));
        return registry.getSchema(
                SchemaLocation.of(schemaBundle.rootSchemaId()));
    }

    private static StructuralValidationFailure unsupportedVersion(
            OpenApiVersion version) {
        return new StructuralValidationFailure(List.of(new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.UNSUPPORTED_STRUCTURE_VERSION,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.STRUCTURAL_VALIDATION,
                "No structural schema is available for OpenAPI "
                        + version.value(),
                VERSION_PATH)));
    }

    private static OpenApiDiagnostic toDiagnostic(Error error) {
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.INVALID_STRUCTURE,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.STRUCTURAL_VALIDATION,
                messageOf(error),
                pathOf(error));
    }

    private static String messageOf(Error error) {
        String message = error.getMessage();
        return message == null || message.isBlank()
                ? "The OpenAPI document has an invalid structure"
                : message;
    }

    private static DocumentPath pathOf(Error error) {
        DocumentPath path =
                DocumentPath.parse(error.getInstanceLocation().toString());
        String property = error.getProperty();
        if (property != null && !property.isBlank()) {
            return path.child(property);
        }
        Integer index = error.getIndex();
        if (index != null) {
            return path.child(index.toString());
        }
        return path;
    }
}
