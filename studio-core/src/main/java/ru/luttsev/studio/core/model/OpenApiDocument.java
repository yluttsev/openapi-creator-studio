package ru.luttsev.studio.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.tag.Tag;

public final class OpenApiDocument extends ExtensibleObject {

    private OpenApiVersion openApiVersion;
    private UriReference self;
    private Info info;
    private UriReference jsonSchemaDialect;
    private List<Server> servers = new ArrayList<>();
    private Paths paths;
    private Map<String, PathItem> webhooks = new LinkedHashMap<>();
    private Components components;
    private List<SecurityRequirement> security = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private ExternalDocumentation externalDocs;

    public OpenApiVersion getOpenApiVersion() {
        return this.openApiVersion;
    }

    public UriReference getSelf() {
        return this.self;
    }

    public Info getInfo() {
        return this.info;
    }

    public UriReference getJsonSchemaDialect() {
        return this.jsonSchemaDialect;
    }

    public List<Server> getServers() {
        return this.servers;
    }

    public Paths getPaths() {
        return this.paths;
    }

    public Map<String, PathItem> getWebhooks() {
        return this.webhooks;
    }

    public Components getComponents() {
        return this.components;
    }

    public List<SecurityRequirement> getSecurity() {
        return this.security;
    }

    public List<Tag> getTags() {
        return this.tags;
    }

    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    public void setOpenApiVersion(OpenApiVersion openApiVersion) {
        this.openApiVersion = openApiVersion;
    }

    public void setSelf(UriReference self) {
        this.self = self;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public void setJsonSchemaDialect(UriReference jsonSchemaDialect) {
        this.jsonSchemaDialect = jsonSchemaDialect;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    public void setWebhooks(Map<String, PathItem> webhooks) {
        this.webhooks = webhooks;
    }

    public void setComponents(Components components) {
        this.components = components;
    }

    public void setSecurity(List<SecurityRequirement> security) {
        this.security = security;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    public OpenApiDocument() {
    }
}
