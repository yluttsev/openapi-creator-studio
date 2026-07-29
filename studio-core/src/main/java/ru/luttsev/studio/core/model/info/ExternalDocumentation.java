package ru.luttsev.studio.core.model.info;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;

@Getter
@Setter
@NoArgsConstructor
public final class ExternalDocumentation extends ExtensibleObject {

    private String description;
    private UriReference url;
}
