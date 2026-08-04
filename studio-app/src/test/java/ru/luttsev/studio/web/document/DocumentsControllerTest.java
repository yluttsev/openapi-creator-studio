package ru.luttsev.studio.web.document;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.luttsev.studio.application.document.DocumentLifecycleService;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.document.DocumentRepresentationService;
import ru.luttsev.studio.application.document.OpenedDocument;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.testsupport.ClasspathResources;
import ru.luttsev.studio.testsupport.OpenApiDocuments;
import ru.luttsev.studio.web.command.CommandResponseMapperImpl;

@WebMvcTest(DocumentsController.class)
@Import({
        DocumentResponseMapperImpl.class,
        DiagnosticMapperImpl.class,
        CommandResponseMapperImpl.class
})
class DocumentsControllerTest {

    private static final String DOCUMENTS = "/api/v1/documents";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentLifecycleService lifecycleService;

    @MockitoBean
    private DocumentRepresentationService representationService;

    @Test
    void createsDocumentAndReturnsLocationAndEtag() throws Exception {
        DocumentSession session = new DocumentSession(
                UUID.randomUUID(), 0, OpenApiDocuments.blank());
        when(lifecycleService.create(
                eq(OpenApiVersion.V3_1_2), eq("Users API"), eq("1.0.0"), isNull()))
                .thenReturn(new OpenedDocument(session, List.of()));

        mockMvc.perform(post(DOCUMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("create-document.json")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ETAG, "\"0\""))
                .andExpect(header().string(
                        HttpHeaders.LOCATION, DOCUMENTS + "/" + session.id()))
                .andExpect(jsonPath("$.id").value(session.id().toString()))
                .andExpect(jsonPath("$.title").value("Test API"))
                .andExpect(jsonPath("$.openApiVersion").value("3.1.2"));
    }

    @Test
    void rejectsCreateRequestMissingTitle() throws Exception {
        mockMvc.perform(post(DOCUMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("create-document-without-title.json")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.violations[0].field").value("title"));
    }

    @Test
    void rejectsMalformedRequestBody() throws Exception {
        mockMvc.perform(post(DOCUMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("malformed-request.json")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST_BODY"));
    }

    @Test
    void returnsCurrentDocumentRepresentation() throws Exception {
        DocumentSession session = new DocumentSession(
                UUID.randomUUID(), 3, OpenApiDocuments.blank());
        when(lifecycleService.get(session.id())).thenReturn(session);
        when(representationService.represent(session.document()))
                .thenReturn(Map.of("openapi", "3.1.2"));

        mockMvc.perform(get(DOCUMENTS + "/" + session.id()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"3\""))
                .andExpect(jsonPath("$.revision").value(3))
                .andExpect(jsonPath("$.document.openapi").value("3.1.2"));
    }

    @Test
    void returnsNotFoundForUnknownDocument() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(lifecycleService.get(documentId))
                .thenThrow(new DocumentNotFoundException(documentId));

        mockMvc.perform(get(DOCUMENTS + "/" + documentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
    }

    @Test
    void closesDocumentAtExpectedRevision() throws Exception {
        UUID documentId = UUID.randomUUID();

        mockMvc.perform(delete(DOCUMENTS + "/" + documentId)
                        .header(HttpHeaders.IF_MATCH, "\"0\""))
                .andExpect(status().isNoContent());

        verify(lifecycleService).close(documentId, 0L);
    }

    @Test
    void requiresIfMatchHeaderToCloseDocument() throws Exception {
        mockMvc.perform(delete(DOCUMENTS + "/" + UUID.randomUUID()))
                .andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.code").value("REVISION_REQUIRED"));
    }

    private String resource(String fileName) {
        return ClasspathResources.readString("/http/" + fileName);
    }
}
