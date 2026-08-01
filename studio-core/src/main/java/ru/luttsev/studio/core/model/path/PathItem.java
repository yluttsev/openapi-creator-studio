package ru.luttsev.studio.core.model.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.ReferenceHolder;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.server.Server;

public final class PathItem extends ExtensibleObject implements ReferenceHolder {

    private UriReference ref;
    private String summary;
    private String description;
    private Map<HttpMethod, Operation> operations = new LinkedHashMap<>();
    private List<Server> servers = new ArrayList<>();
    private List<ReferenceOr<Parameter>> parameters = new ArrayList<>();

    public UriReference getRef() {
        return this.ref;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public Map<HttpMethod, Operation> getOperations() {
        return this.operations;
    }

    public List<Server> getServers() {
        return this.servers;
    }

    public List<ReferenceOr<Parameter>> getParameters() {
        return this.parameters;
    }

    public void setRef(UriReference ref) {
        this.ref = ref;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOperations(Map<HttpMethod, Operation> operations) {
        this.operations = operations;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public void setParameters(List<ReferenceOr<Parameter>> parameters) {
        this.parameters = parameters;
    }

    public PathItem() {
    }
}
