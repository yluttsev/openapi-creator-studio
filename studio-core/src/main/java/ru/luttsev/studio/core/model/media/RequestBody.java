package ru.luttsev.studio.core.model.media;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

@Getter
@Setter
@NoArgsConstructor
public final class RequestBody extends ExtensibleObject {

    private String description;
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();
    private Boolean required;
}
