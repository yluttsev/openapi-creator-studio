package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.component.RenameComponentCommand;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.generated.model.RenameComponentCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class RenameComponentCommandHandler
        implements DocumentCommandRequestHandler<RenameComponentCommandRequest> {

    @Override
    public Class<RenameComponentCommandRequest> requestType() {
        return RenameComponentCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(RenameComponentCommandRequest request) {
        return new RenameComponentCommand(
                DocumentPath.parse(request.getPayload().getComponentPath()),
                request.getPayload().getNewName());
    }
}
