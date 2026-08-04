package ru.luttsev.studio.web.exporting;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.exporting.DocumentExport;
import ru.luttsev.studio.application.exporting.DocumentExportService;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.testsupport.ClasspathResources;
import ru.luttsev.studio.web.command.CommandResponseMapperImpl;
import ru.luttsev.studio.web.document.DiagnosticMapperImpl;

@WebMvcTest(ExportController.class)
@Import({
        ExportMapperImpl.class,
        DiagnosticMapperImpl.class,
        CommandResponseMapperImpl.class
})
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentExportService exportService;

    @Test
    void exportsDocumentAndReturnsCurrentEtag() throws Exception {
        UUID documentId = UUID.randomUUID();
        DocumentExport export = new DocumentExport(
                2,
                OpenApiFormat.YAML,
                OpenApiVersion.V3_1_2,
                "openapi.yaml",
                "openapi: 3.1.2\n",
                List.of());
        when(exportService.export(eq(documentId), eq(OpenApiFormat.YAML), isNull()))
                .thenReturn(export);

        mockMvc.perform(post("/api/v1/documents/" + documentId + "/exports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ClasspathResources.readString(
                                "/http/export-yaml.json")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(jsonPath("$.format").value("YAML"))
                .andExpect(jsonPath("$.targetVersion").value("3.1.2"))
                .andExpect(jsonPath("$.fileName").value("openapi.yaml"))
                .andExpect(jsonPath("$.content").value("openapi: 3.1.2\n"));
    }

    @Test
    void returnsNotFoundForUnknownDocument() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(exportService.export(eq(documentId), eq(OpenApiFormat.YAML), isNull()))
                .thenThrow(new DocumentNotFoundException(documentId));

        mockMvc.perform(post("/api/v1/documents/" + documentId + "/exports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ClasspathResources.readString(
                                "/http/export-yaml.json")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
    }
}
