package ru.luttsev.studio.core.model.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.server.Server;

@Getter
@Setter
@NoArgsConstructor
public final class Operation extends ExtensibleObject {

    private List<String> tags = new ArrayList<>();
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String operationId;
    private List<ReferenceOr<Parameter>> parameters = new ArrayList<>();
    private ReferenceOr<RequestBody> requestBody;
    private Map<ResponseKey, ReferenceOr<ApiResponse>> responses = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Callback>> callbacks = new LinkedHashMap<>();
    private Boolean deprecated;
    private List<SecurityRequirement> security = new ArrayList<>();
    private List<Server> servers = new ArrayList<>();
}
