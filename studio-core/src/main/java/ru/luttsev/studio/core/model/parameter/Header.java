package ru.luttsev.studio.core.model.parameter;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
@Setter
@NoArgsConstructor
public final class Header extends ExtensibleObject {

    private String description;
    private Boolean required;
    private Boolean deprecated;
    private ParameterStyle style;
    private Boolean explode;
    private Schema schema;
    private DocumentValue example;
    private Map<String, ReferenceOr<Example>> examples = new LinkedHashMap<>();
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();
}
