package ru.luttsev.studio.web.validation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import ru.luttsev.studio.application.validation.DocumentValidation;
import ru.luttsev.studio.application.validation.DocumentValidationService;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.web.command.CommandResponseMapperImpl;
import ru.luttsev.studio.web.document.DiagnosticMapperImpl;

@WebMvcTest(ValidationController.class)
@Import({
        ValidationResponseMapperImpl.class,
        DiagnosticMapperImpl.class,
        CommandResponseMapperImpl.class
})
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentValidationService validationService;

    @Test
    void returnsValidResultForConsistentDocument() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(validationService.validate(documentId))
                .thenReturn(new DocumentValidation(4, true, List.of()));

        mockMvc.perform(get("/api/v1/documents/" + documentId + "/validation"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
                .andExpect(jsonPath("$.revision").value(4))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.diagnostics").isEmpty());
    }

    @Test
    void returnsIssuesForInvalidDocument() throws Exception {
        UUID documentId = UUID.randomUUID();
        ValidationIssue issue = new ValidationIssue(
                new ValidationCode("document.openapi-version.missing"),
                ValidationSeverity.ERROR,
                "OpenAPI version is missing",
                DocumentPath.parse("/openapi"));
        when(validationService.validate(documentId))
                .thenReturn(new DocumentValidation(0, false, List.of(issue)));

        mockMvc.perform(get("/api/v1/documents/" + documentId + "/validation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.diagnostics[0].code")
                        .value("document.openapi-version.missing"))
                .andExpect(jsonPath("$.diagnostics[0].path").value("/openapi"))
                .andExpect(jsonPath("$.diagnostics[0].phase")
                        .value("SEMANTIC_VALIDATION"));
    }

    @Test
    void returnsNotFoundForUnknownDocument() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(validationService.validate(documentId))
                .thenThrow(new DocumentNotFoundException(documentId));

        mockMvc.perform(get("/api/v1/documents/" + documentId + "/validation"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
    }
}
