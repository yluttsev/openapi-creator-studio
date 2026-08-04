package ru.luttsev.studio.web.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import ru.luttsev.studio.application.command.CommandRejectedException;
import ru.luttsev.studio.application.command.DocumentCommandService;
import ru.luttsev.studio.application.command.ExecutedDocumentCommand;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.result.CommandCode;
import ru.luttsev.studio.core.command.result.CommandIssue;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.testsupport.ClasspathResources;
import ru.luttsev.studio.web.document.DiagnosticMapperImpl;

@WebMvcTest(CommandsController.class)
@Import({
        CommandResponseMapperImpl.class,
        DiagnosticMapperImpl.class
})
class CommandsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentCommandDispatcher dispatcher;

    @MockitoBean
    private DocumentCommandService commandService;

    @Test
    void executesCommandAndReturnsNewRevision() throws Exception {
        UUID documentId = UUID.randomUUID();
        DocumentCommand command = mock(DocumentCommand.class);
        when(dispatcher.dispatch(any())).thenReturn(command);
        when(commandService.execute(eq(documentId), eq(0L), eq(command)))
                .thenReturn(new ExecutedDocumentCommand(
                        1, List.of(DocumentPath.parse("/paths/~1users"))));

        mockMvc.perform(post("/api/v1/documents/" + documentId + "/commands")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("add-users-path-command.json")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.changedPaths[0]").value("/paths/~1users"));
    }

    @Test
    void requiresIfMatchHeader() throws Exception {
        mockMvc.perform(post("/api/v1/documents/" + UUID.randomUUID() + "/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("add-users-path-command.json")))
                .andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.code").value("REVISION_REQUIRED"));
    }

    @Test
    void returnsConflictWhenCommandIsRejected() throws Exception {
        UUID documentId = UUID.randomUUID();
        DocumentCommand command = mock(DocumentCommand.class);
        when(dispatcher.dispatch(any())).thenReturn(command);
        when(commandService.execute(eq(documentId), eq(0L), eq(command)))
                .thenThrow(new CommandRejectedException(List.of(new CommandIssue(
                        new CommandCode("path.already-exists"),
                        "Path already exists",
                        DocumentPath.parse("/paths/~1users")))));

        mockMvc.perform(post("/api/v1/documents/" + documentId + "/commands")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resource("add-users-path-command.json")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMAND_REJECTED"))
                .andExpect(jsonPath("$.issues").isNotEmpty());
    }

    private String resource(String fileName) {
        return ClasspathResources.readString("/http/" + fileName);
    }
}
