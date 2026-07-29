package ru.luttsev.studio.core.model.info;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

@Getter
@Setter
@NoArgsConstructor
public final class License extends ExtensibleObject {

    private String name;
    private String identifier;
    private UriReference url;
}
