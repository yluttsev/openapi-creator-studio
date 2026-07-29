package ru.luttsev.studio.core.model.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;

@Getter
@Setter
@NoArgsConstructor
public final class OAuthFlows extends ExtensibleObject {

    private OAuthFlow implicit;
    private OAuthFlow password;
    private OAuthFlow clientCredentials;
    private OAuthFlow authorizationCode;
    private OAuthFlow deviceAuthorization;
}
