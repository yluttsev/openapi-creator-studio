package ru.luttsev.studio.core.model.response;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

@Getter
@Setter
@NoArgsConstructor
public final class ApiResponse extends ExtensibleObject {

    private String description;
    private Map<String, ReferenceOr<Header>> headers = new LinkedHashMap<>();
    private Map<MediaTypeName, ReferenceOr<MediaType>> content = new LinkedHashMap<>();
    private Map<String, ReferenceOr<Link>> links = new LinkedHashMap<>();
}
