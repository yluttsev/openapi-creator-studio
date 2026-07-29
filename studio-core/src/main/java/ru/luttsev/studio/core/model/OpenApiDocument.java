package ru.luttsev.studio.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.tag.Tag;

@Getter
@Setter
@NoArgsConstructor
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
}
