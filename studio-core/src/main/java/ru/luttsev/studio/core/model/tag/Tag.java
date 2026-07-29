package ru.luttsev.studio.core.model.tag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.info.ExternalDocumentation;

@Getter
@Setter
@NoArgsConstructor
public final class Tag extends ExtensibleObject {

    private String name;
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String parent;
    private String kind;
}
