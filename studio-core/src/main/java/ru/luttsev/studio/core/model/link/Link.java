package ru.luttsev.studio.core.model.link;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
@Setter
@NoArgsConstructor
public final class Link extends ExtensibleObject {

    private UriReference operationRef;
    private String operationId;
    private Map<String, DocumentValue> parameters = new LinkedHashMap<>();
    private DocumentValue requestBody;
    private String description;
    private Server server;
}
