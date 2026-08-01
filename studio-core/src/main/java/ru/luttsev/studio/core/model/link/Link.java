package ru.luttsev.studio.core.model.link;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.value.DocumentValue;

public final class Link extends ExtensibleObject {

    private UriReference operationRef;
    private String operationId;
    private Map<String, DocumentValue> parameters = new LinkedHashMap<>();
    private DocumentValue requestBody;
    private String description;
    private Server server;

    public UriReference getOperationRef() {
        return this.operationRef;
    }

    public String getOperationId() {
        return this.operationId;
    }

    public Map<String, DocumentValue> getParameters() {
        return this.parameters;
    }

    public DocumentValue getRequestBody() {
        return this.requestBody;
    }

    public String getDescription() {
        return this.description;
    }

    public Server getServer() {
        return this.server;
    }

    public void setOperationRef(UriReference operationRef) {
        this.operationRef = operationRef;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setParameters(Map<String, DocumentValue> parameters) {
        this.parameters = parameters;
    }

    public void setRequestBody(DocumentValue requestBody) {
        this.requestBody = requestBody;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Link() {
    }
}
