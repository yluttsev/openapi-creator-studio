package ru.luttsev.studio.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class StudioHttpIntegrationTest {

    private static final String DOCUMENTS = "/api/v1/documents";

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    @Test
    void executesCompleteDocumentLifecycle() throws Exception {
        String documentUri = createDocument();

        mockMvc.perform(get(documentUri))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"0\""))
                .andExpect(jsonPath("$.revision").value(0))
                .andExpect(jsonPath("$.document.openapi").value("3.1.2"));

        mockMvc.perform(post(documentUri + "/commands")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("add-users-path-command.json")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.changedPaths[0]")
                        .value("/paths/~1users"));

        mockMvc.perform(get(documentUri + "/validation"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.valid").value(true));

        mockMvc.perform(post(documentUri + "/exports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("export-yaml.json")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.format").value("YAML"))
                .andExpect(jsonPath("$.targetVersion").value("3.1.2"))
                .andExpect(jsonPath("$.fileName").value("openapi.yaml"))
                .andExpect(jsonPath("$.content").value(
                        Matchers.containsString("openapi: 3.1.2")));

        mockMvc.perform(delete(documentUri)
                        .header(HttpHeaders.IF_MATCH, "\"1\""))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(documentUri))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
    }

    @Test
    void importsAndExportsOpenApiDocument() throws Exception {
        MvcResult imported = mockMvc.perform(post(DOCUMENTS + "/import")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(resource("valid-openapi.yaml")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ETAG, "\"0\""))
                .andExpect(jsonPath("$.title").value("Imported API"))
                .andReturn();
        String documentUri = imported.getResponse().getHeader(HttpHeaders.LOCATION);

        mockMvc.perform(post(documentUri + "/exports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("export-json-3.1.1.json")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetVersion").value("3.1.1"))
                .andExpect(jsonPath("$.fileName").value("openapi.json"))
                .andExpect(jsonPath("$.content").value(
                        Matchers.containsString(
                                "\"openapi\" : \"3.1.1\"")));

        mockMvc.perform(post(DOCUMENTS + "/import")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(resource("invalid-openapi.yaml")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code")
                        .value("OPENAPI_PROCESSING_FAILED"))
                .andExpect(jsonPath("$.diagnostics").isNotEmpty());
    }

    @Test
    void returnsContractualProblemDetailsForRequestFailures() throws Exception {
        mockMvc.perform(post(DOCUMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("malformed-request.json")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST_BODY"));

        mockMvc.perform(post(DOCUMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource(
                                "create-document-without-title.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("REQUEST_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.violations[0].field").value("title"));

        String documentUri = createDocument();
        mockMvc.perform(delete(documentUri))
                .andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.code").value("REVISION_REQUIRED"));

        mockMvc.perform(delete(documentUri)
                        .header(HttpHeaders.IF_MATCH, "\"7\""))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.code").value("REVISION_MISMATCH"));

        addPath(documentUri, "\"0\"")
                .andExpect(status().isOk());
        addPath(documentUri, "\"1\"")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMAND_REJECTED"))
                .andExpect(jsonPath("$.issues").isNotEmpty());

        mockMvc.perform(get(DOCUMENTS + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
    }

    private String createDocument() throws Exception {
        MvcResult result = mockMvc.perform(post(DOCUMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("create-document.json")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ETAG, "\"0\""))
                .andExpect(jsonPath("$.revision").value(0))
                .andExpect(jsonPath("$.openApiVersion").value("3.1.2"))
                .andReturn();
        return result.getResponse().getHeader(HttpHeaders.LOCATION);
    }

    private ResultActions addPath(
            String documentUri,
            String revision) throws Exception {
        return mockMvc.perform(post(documentUri + "/commands")
                .header(HttpHeaders.IF_MATCH, revision)
                .contentType(MediaType.APPLICATION_JSON)
                .content(resource("add-users-path-command.json")));
    }

    private String resource(String fileName) throws IOException {
        String path = "/http/" + fileName;
        try (InputStream input = getClass().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Test resource not found: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
