package ru.luttsev.studio.openapi.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticCode;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.exporting.DefaultOpenApiExporter;
import ru.luttsev.studio.openapi.exporting.ExportOptions;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.openapi.importing.DefaultOpenApiImporter;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.ExportFailure;
import ru.luttsev.studio.openapi.result.ExportResult;
import ru.luttsev.studio.openapi.result.ExportSuccess;
import ru.luttsev.studio.openapi.result.ImportFailure;
import ru.luttsev.studio.openapi.result.ImportResult;
import ru.luttsev.studio.openapi.result.ImportSuccess;
import ru.luttsev.studio.openapi.version.v31.encode.OpenApi31Encoder;

class OpenApiImportExportIntegrationTest {

    private static final String VALID_YAML = """
            openapi: 3.1.2
            info:
              title: Users API
              version: 1.0.0
            paths:
              /users:
                get:
                  operationId: listUsers
                  tags: [users]
                  responses:
                    "200":
                      description: Users
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              $ref: "#/components/schemas/User"
            components:
              schemas:
                User:
                  type: object
                  properties:
                    id: { type: string }
            tags:
              - name: users
                description: User operations
            """;

    private final DefaultOpenApiImporter importer =
            new DefaultOpenApiImporter();
    private final DefaultOpenApiExporter exporter =
            new DefaultOpenApiExporter();

    @Test
    void roundTripsYamlAndJsonThroughCompletePipelines() {
        ImportSuccess initialImport = importSuccess(
                importer.importDocument(
                        VALID_YAML,
                        ImportOptions.autoDetect()));

        ExportSuccess yamlExport = exportSuccess(exporter.exportDocument(
                initialImport.document(),
                new ExportOptions(
                        OpenApiFormat.YAML,
                        OpenApiVersion.V3_1_2)));
        ImportSuccess yamlImport = importSuccess(importer.importDocument(
                yamlExport.content(),
                ImportOptions.autoDetect()));

        ExportSuccess jsonExport = exportSuccess(exporter.exportDocument(
                yamlImport.document(),
                new ExportOptions(
                        OpenApiFormat.JSON,
                        OpenApiVersion.V3_1_2)));
        assertTrue(jsonExport.content().stripLeading().startsWith("{"));
        ImportSuccess jsonImport = importSuccess(importer.importDocument(
                jsonExport.content(),
                ImportOptions.autoDetect()));

        assertEquals(
                encode(initialImport.document()),
                encode(jsonImport.document()));
        assertTrue(initialImport.diagnostics().isEmpty());
        assertTrue(yamlExport.diagnostics().isEmpty());
        assertTrue(jsonExport.diagnostics().isEmpty());
    }

    @Test
    void rejectsStructurallyInvalidImportBeforeMapping() {
        ImportFailure failure = assertInstanceOf(
                ImportFailure.class,
                importer.importDocument(
                        "openapi: 3.1.2\npaths: {}\n",
                        ImportOptions.autoDetect()));

        assertTrue(failure.diagnostics().stream().allMatch(diagnostic ->
                diagnostic.phase()
                        == DiagnosticPhase.STRUCTURAL_VALIDATION));
    }

    @Test
    void rejectsSemanticallyInvalidImport() {
        ImportFailure failure = assertInstanceOf(
                ImportFailure.class,
                importer.importDocument("""
                        openapi: 3.1.2
                        info:
                          title: Broken API
                          version: 1.0.0
                        paths:
                          /users:
                            get:
                              responses:
                                "200":
                                  $ref: "#/components/responses/Missing"
                        """, ImportOptions.autoDetect()));

        assertTrue(failure.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.phase() == DiagnosticPhase.SEMANTIC_VALIDATION
                        && diagnostic.path().toPointer().equals(
                                "/paths/~1users/get/responses/200/$ref")));
    }

    @Test
    void keepsSemanticWarningsInSuccessfulResults() {
        String externalReferenceDocument = VALID_YAML.replace(
                "#/components/schemas/User",
                "https://example.com/schemas/User");

        ImportSuccess imported = importSuccess(importer.importDocument(
                externalReferenceDocument,
                ImportOptions.autoDetect()));
        assertTrue(imported.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.phase() == DiagnosticPhase.SEMANTIC_VALIDATION
                        && diagnostic.severity()
                                == DiagnosticSeverity.WARNING));

        ExportSuccess exported = exportSuccess(exporter.exportDocument(
                imported.document(),
                new ExportOptions(
                        OpenApiFormat.YAML,
                        OpenApiVersion.V3_1_2)));
        assertTrue(exported.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.phase() == DiagnosticPhase.SEMANTIC_VALIDATION
                        && diagnostic.severity()
                                == DiagnosticSeverity.WARNING));
    }

    @Test
    void rejectsVersionIncompatibleExport() {
        OpenApiDocument document = importSuccess(importer.importDocument(
                VALID_YAML,
                ImportOptions.autoDetect())).document();
        document.setSelf(
                new UriReference("https://example.com/openapi.yaml"));

        ExportFailure failure = assertInstanceOf(
                ExportFailure.class,
                exporter.exportDocument(
                        document,
                        new ExportOptions(
                                OpenApiFormat.YAML,
                                OpenApiVersion.V3_1_2)));

        assertDiagnostic(
                failure.diagnostics(),
                OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD,
                DiagnosticPhase.VERSION_COMPATIBILITY,
                "/self");
    }

    @Test
    void structurallyValidatesEncodedDocumentBeforeSerialization() {
        OpenApiDocument document = new OpenApiDocument();
        document.setOpenApiVersion(OpenApiVersion.V3_1_2);
        document.setPaths(new Paths());

        ExportFailure failure = assertInstanceOf(
                ExportFailure.class,
                exporter.exportDocument(
                        document,
                        new ExportOptions(
                                OpenApiFormat.JSON,
                                OpenApiVersion.V3_1_2)));

        assertTrue(failure.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.phase()
                        == DiagnosticPhase.STRUCTURAL_VALIDATION));
    }

    @Test
    void rejectsExportWithoutTargetVersionAdapter() {
        OpenApiDocument document = importSuccess(importer.importDocument(
                VALID_YAML,
                ImportOptions.autoDetect())).document();

        ExportFailure failure = assertInstanceOf(
                ExportFailure.class,
                exporter.exportDocument(
                        document,
                        new ExportOptions(
                                OpenApiFormat.JSON,
                                OpenApiVersion.V3_0_4)));

        assertDiagnostic(
                failure.diagnostics(),
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                DiagnosticPhase.VERSION_COMPATIBILITY,
                "/openapi");
    }

    @Test
    void rejectsImportWithoutDetectedVersionAdapter() {
        ImportFailure failure = assertInstanceOf(
                ImportFailure.class,
                importer.importDocument("""
                        openapi: 3.0.4
                        info:
                          title: Legacy API
                          version: 1.0.0
                        paths: {}
                        """, ImportOptions.autoDetect()));

        assertDiagnostic(
                failure.diagnostics(),
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                DiagnosticPhase.MAPPING,
                "/openapi");
    }

    private static ObjectValue encode(OpenApiDocument document) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                new OpenApi31Encoder().encode(
                        document,
                        OpenApiVersion.V3_1_2));
        return assertInstanceOf(ObjectValue.class, success.value());
    }

    private static ImportSuccess importSuccess(ImportResult result) {
        return assertInstanceOf(ImportSuccess.class, result);
    }

    private static ExportSuccess exportSuccess(ExportResult result) {
        return assertInstanceOf(ExportSuccess.class, result);
    }

    private static void assertDiagnostic(
            List<OpenApiDiagnostic> diagnostics,
            DiagnosticCode code,
            DiagnosticPhase phase,
            String path) {
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.code().equals(code)
                        && diagnostic.phase() == phase
                        && diagnostic.path().toPointer().equals(path)));
    }
}
