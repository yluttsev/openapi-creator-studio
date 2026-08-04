package ru.luttsev.studio.web.command.handler;

import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.core.command.schema.RemoveSchemaPropertyCommand;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.generated.model.RemoveSchemaPropertyCommandRequest;
import ru.luttsev.studio.web.command.DocumentCommandRequestHandler;

@Component
public final class RemoveSchemaPropertyCommandHandler
        implements DocumentCommandRequestHandler<RemoveSchemaPropertyCommandRequest> {

    @Override
    public Class<RemoveSchemaPropertyCommandRequest> requestType() {
        return RemoveSchemaPropertyCommandRequest.class;
    }

    @Override
    public DocumentCommand createCommand(
            RemoveSchemaPropertyCommandRequest request) {
        return new RemoveSchemaPropertyCommand(
                DocumentPath.parse(request.getPayload().getSchemaPath()),
                request.getPayload().getPropertyName());
    }
}
