package ru.luttsev.studio.core.model.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
@Setter
@NoArgsConstructor
public final class Example extends ExtensibleObject {

    private String summary;
    private String description;
    private DocumentValue dataValue;
    private String serializedValue;
    private UriReference externalValue;
    private DocumentValue value;
}
