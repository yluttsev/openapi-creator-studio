package ru.luttsev.studio.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.security.SecurityScheme;

public final class Components extends ExtensibleObject {

    private Map<String, Schema> schemas = new LinkedHashMap<>();
    private Map<String, ReferenceOr<ApiResponse>> responses = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Parameter>> parameters = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Example>> examples = new LinkedHashMap<>();
    private Map<String, ReferenceOr<RequestBody>> requestBodies = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Header>> headers = new LinkedHashMap<>();
    private Map<String, ReferenceOr<SecurityScheme>> securitySchemes = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Link>> links = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Callback>> callbacks = new LinkedHashMap<>();
    private Map<String, PathItem> pathItems = new LinkedHashMap<>();
    private Map<String, ReferenceOr<MediaType>> mediaTypes = new LinkedHashMap<>();

    public Map<String, Schema> getSchemas() {
        return this.schemas;
    }

    public Map<String, ReferenceOr<ApiResponse>> getResponses() {
        return this.responses;
    }

    public Map<String, ReferenceOr<Parameter>> getParameters() {
        return this.parameters;
    }

    public Map<String, ReferenceOr<Example>> getExamples() {
        return this.examples;
    }

    public Map<String, ReferenceOr<RequestBody>> getRequestBodies() {
        return this.requestBodies;
    }

    public Map<String, ReferenceOr<Header>> getHeaders() {
        return this.headers;
    }

    public Map<String, ReferenceOr<SecurityScheme>> getSecuritySchemes() {
        return this.securitySchemes;
    }

    public Map<String, ReferenceOr<Link>> getLinks() {
        return this.links;
    }

    public Map<String, ReferenceOr<Callback>> getCallbacks() {
        return this.callbacks;
    }

    public Map<String, PathItem> getPathItems() {
        return this.pathItems;
    }

    public Map<String, ReferenceOr<MediaType>> getMediaTypes() {
        return this.mediaTypes;
    }

    public void setSchemas(Map<String, Schema> schemas) {
        this.schemas = schemas;
    }

    public void setResponses(Map<String, ReferenceOr<ApiResponse>> responses) {
        this.responses = responses;
    }

    public void setParameters(Map<String, ReferenceOr<Parameter>> parameters) {
        this.parameters = parameters;
    }

    public void setExamples(Map<String, ReferenceOr<Example>> examples) {
        this.examples = examples;
    }

    public void setRequestBodies(Map<String, ReferenceOr<RequestBody>> requestBodies) {
        this.requestBodies = requestBodies;
    }

    public void setHeaders(Map<String, ReferenceOr<Header>> headers) {
        this.headers = headers;
    }

    public void setSecuritySchemes(Map<String, ReferenceOr<SecurityScheme>> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }

    public void setLinks(Map<String, ReferenceOr<Link>> links) {
        this.links = links;
    }

    public void setCallbacks(Map<String, ReferenceOr<Callback>> callbacks) {
        this.callbacks = callbacks;
    }

    public void setPathItems(Map<String, PathItem> pathItems) {
        this.pathItems = pathItems;
    }

    public void setMediaTypes(Map<String, ReferenceOr<MediaType>> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public Components() {
    }
}
