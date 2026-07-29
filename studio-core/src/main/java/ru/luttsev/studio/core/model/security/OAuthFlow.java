package ru.luttsev.studio.core.model.security;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

@Getter
@Setter
@NoArgsConstructor
public final class OAuthFlow extends ExtensibleObject {

    private UriReference authorizationUrl;
    private UriReference deviceAuthorizationUrl;
    private UriReference tokenUrl;
    private UriReference refreshUrl;
    private Map<String, String> scopes = new LinkedHashMap<>();
}
