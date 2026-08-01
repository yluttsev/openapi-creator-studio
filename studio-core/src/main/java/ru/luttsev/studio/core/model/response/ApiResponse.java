package ru.luttsev.studio.core.model.response;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

public final class ApiResponse extends ExtensibleObject {

    private String description;
    private Map<String, ReferenceOr<Header>> headers = new LinkedHashMap<>();
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Link>> links = new LinkedHashMap<>();

    public String getDescription() {
        return this.description;
    }

    public Map<String, ReferenceOr<Header>> getHeaders() {
        return this.headers;
    }

    public Map<MediaTypeName, ReferenceOr<MediaType>> getContent() {
        return this.content;
    }

    public Map<String, ReferenceOr<Link>> getLinks() {
        return this.links;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHeaders(Map<String, ReferenceOr<Header>> headers) {
        this.headers = headers;
    }

    public void setContent(Map<MediaTypeName, ReferenceOr<MediaType>> content) {
        this.content = content;
    }

    public void setLinks(Map<String, ReferenceOr<Link>> links) {
        this.links = links;
    }

    public ApiResponse() {
    }
}
