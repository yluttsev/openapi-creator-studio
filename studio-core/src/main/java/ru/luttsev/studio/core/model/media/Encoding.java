package ru.luttsev.studio.core.model.media;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.ParameterStyle;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

@Getter
@Setter
@NoArgsConstructor
public final class Encoding extends ExtensibleObject {

    private String contentType;
    private Map<String, ReferenceOr<Header>> headers = new LinkedHashMap<>();
    private ParameterStyle style;
    private Boolean explode;
    private Boolean allowReserved;
    private Map<String, Encoding> encoding = new LinkedHashMap<>();
    private List<Encoding> prefixEncoding = new ArrayList<>();
    private Encoding itemEncoding;
}
