package ru.luttsev.studio.application.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.infrastructure.workspace.InMemoryDocumentWorkspace;
import ru.luttsev.studio.openapi.importing.DefaultOpenApiImporter;
import ru.luttsev.studio.openapi.version.OpenApiVersionAdapters;
import ru.luttsev.studio.testsupport.ClasspathResources;

class DocumentLifecycleServiceTest {

    private DocumentLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new DocumentLifecycleService(
                new InMemoryDocumentWorkspace(),
                new OpenApiDocumentFactory(),
                new DefaultOpenApiImporter(),
                OpenApiVersionAdapters.defaults());
    }

    @Test
    void createsAndClosesBlankDocument() {
        OpenedDocument opened = service.create(
                OpenApiVersion.V3_1_2,
                "Orders API",
                "1.0.0",
                "Orders");

        assertThat(opened.session().revision()).isZero();
        assertThat(opened.session().document().getInfo().getTitle())
                .isEqualTo("Orders API");
        assertThat(opened.session().document().getInfo().getDescription())
                .isEqualTo("Orders");

        service.close(opened.session().id(), opened.session().revision());
        assertThatThrownBy(() -> service.get(opened.session().id()))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void importsValidYaml() {
        OpenedDocument opened = service.importDocument(
                ClasspathResources.readString("/openapi/valid-openapi.yaml"));

        assertThat(opened.session().document().getInfo().getTitle())
                .isEqualTo("Imported API");
    }

    @Test
    void rejectsInvalidImportWithoutCreatingSession() {
        String invalidYaml =
                ClasspathResources.readString("/openapi/invalid-openapi.yaml");

        assertThatThrownBy(() -> service.importDocument(invalidYaml))
                .isInstanceOf(OpenApiProcessingException.class)
                .satisfies(exception -> assertThat(
                        ((OpenApiProcessingException) exception)
                                .getDiagnostics())
                        .isNotEmpty());
    }

    @Test
    void rejectsUnsupportedVersionForBlankDocument() {
        assertThatThrownBy(() -> service.create(
                OpenApiVersion.V3_0_4,
                "Legacy API",
                "1.0.0",
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported OpenAPI version");
    }
}
