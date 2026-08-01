package ru.luttsev.studio.core.model.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.Responses;
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
    private Responses responses = new Responses();
    private Map<String, ReferenceOr<Callback>> callbacks = new LinkedHashMap<>();
    private Boolean deprecated;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
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
}
