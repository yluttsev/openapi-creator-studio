package ru.luttsev.studio.core.model.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.Responses;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.server.Server;

public final class Operation extends ExtensibleObject {

    private List<String> tags = new ArrayList<>();
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String operationId;
    private List<ReferenceOr<Parameter>> parameters = new ArrayList<>();
    private ReferenceOr<RequestBody> requestBody;
    private Responses responses = new Responses();
    private Map<String, ReferenceOr<Callback>> callbacks = new LinkedHashMap<>();
    private Boolean deprecated;
    private boolean securityOverride;
    private final PresenceAwareList<SecurityRequirement> security =
            new PresenceAwareList<>(this::markSecurityOverride);
    private List<Server> servers = new ArrayList<>();

    public List<SecurityRequirement> getSecurity() {
        return security;
    }

    public void setSecurity(List<SecurityRequirement> requirements) {
        Objects.requireNonNull(requirements, "requirements must not be null");
        security.replaceContents(requirements);
    }

    public boolean hasSecurityOverride() {
        return securityOverride;
    }

    public void inheritSecurity() {
        security.clearSilently();
        securityOverride = false;
    }

    private void markSecurityOverride() {
        securityOverride = true;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    public String getOperationId() {
        return this.operationId;
    }

    public List<ReferenceOr<Parameter>> getParameters() {
        return this.parameters;
    }

    public ReferenceOr<RequestBody> getRequestBody() {
        return this.requestBody;
    }

    public Responses getResponses() {
        return this.responses;
    }

    public Map<String, ReferenceOr<Callback>> getCallbacks() {
        return this.callbacks;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public List<Server> getServers() {
        return this.servers;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setParameters(List<ReferenceOr<Parameter>> parameters) {
        this.parameters = parameters;
    }

    public void setRequestBody(ReferenceOr<RequestBody> requestBody) {
        this.requestBody = requestBody;
    }

    public void setResponses(Responses responses) {
        this.responses = responses;
    }

    public void setCallbacks(Map<String, ReferenceOr<Callback>> callbacks) {
        this.callbacks = callbacks;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public Operation() {
    }
}
