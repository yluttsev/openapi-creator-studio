package ru.luttsev.studio.web.command;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.luttsev.studio.application.command.DocumentCommandService;
import ru.luttsev.studio.application.command.ExecutedDocumentCommand;
import ru.luttsev.studio.generated.api.CommandsApi;
import ru.luttsev.studio.generated.model.CommandResponse;
import ru.luttsev.studio.generated.model.DocumentCommandRequest;
import ru.luttsev.studio.web.document.RevisionHeader;

@RestController
@RequiredArgsConstructor
public class CommandsController implements CommandsApi {

    private final DocumentCommandDispatcher dispatcher;
    private final DocumentCommandService commandService;
    private final CommandResponseMapper responseMapper;

    @Override
    public ResponseEntity<CommandResponse> executeDocumentCommand(
            String ifMatch,
            UUID documentId,
            DocumentCommandRequest request) {
        ExecutedDocumentCommand execution = commandService.execute(
                documentId,
                RevisionHeader.parse(ifMatch),
                dispatcher.dispatch(request));
        return ResponseEntity.ok()
                .eTag(RevisionHeader.format(execution.revision()))
                .body(responseMapper.map(execution));
    }
}
