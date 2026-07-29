package ru.luttsev.studio.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

@Getter
@Setter
@NoArgsConstructor
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
}
