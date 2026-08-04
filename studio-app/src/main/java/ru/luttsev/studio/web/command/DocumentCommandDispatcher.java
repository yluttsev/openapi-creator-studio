package ru.luttsev.studio.web.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.command.DocumentCommand;
import ru.luttsev.studio.generated.model.DocumentCommandRequest;

@Component
public final class DocumentCommandDispatcher {

    private final Map<Class<? extends DocumentCommandRequest>,
            DocumentCommandRequestHandler<?>> handlers;

    public DocumentCommandDispatcher(
            List<DocumentCommandRequestHandler<?>> handlers) {
        LinkedHashMap<Class<? extends DocumentCommandRequest>,
                DocumentCommandRequestHandler<?>> indexedHandlers =
                new LinkedHashMap<>();
        for (DocumentCommandRequestHandler<?> handler : handlers) {
            DocumentCommandRequestHandler<?> previous = indexedHandlers.put(
                    handler.requestType(),
                    handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple command handlers for "
                                + handler.requestType().getName());
            }
        }
        this.handlers = Map.copyOf(indexedHandlers);
    }

    public DocumentCommand dispatch(DocumentCommandRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        DocumentCommandRequestHandler<?> handler = handlers.get(
                request.getClass());
        if (handler == null) {
            throw new IllegalArgumentException(
                    "Unsupported document command request: "
                            + request.getClass().getName());
        }
        return createCommand(handler, request);
    }

    private static <R extends DocumentCommandRequest> DocumentCommand createCommand(
            DocumentCommandRequestHandler<R> handler,
            DocumentCommandRequest request) {
        return handler.createCommand(handler.requestType().cast(request));
    }
}
