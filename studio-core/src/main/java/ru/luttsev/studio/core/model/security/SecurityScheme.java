package ru.luttsev.studio.core.model.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

@Getter
@Setter
@NoArgsConstructor
public final class SecurityScheme extends ExtensibleObject {

    private SecuritySchemeType type;
    private String description;
    private String name;
    private ApiKeyLocation location;
    private String scheme;
    private String bearerFormat;
    private OAuthFlows flows;
    private UriReference openIdConnectUrl;
    private UriReference oauth2MetadataUrl;
    private Boolean deprecated;
}
