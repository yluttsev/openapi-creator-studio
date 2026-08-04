package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.component.DeleteComponentCommand;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.generated.model.DeleteComponentCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class DeleteComponentCommandHandler
        implements DocumentCommandRequestHandler<DeleteComponentCommandRequest> {

    @Override
    public Class<DeleteComponentCommandRequest> requestType() {
        return DeleteComponentCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(DeleteComponentCommandRequest request) {
        return new DeleteComponentCommand(
                DocumentPath.parse(request.getPayload().getComponentPath()));
    }
}
