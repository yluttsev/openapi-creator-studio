package ru.luttsev.studio.core.model.media;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
@Setter
@NoArgsConstructor
public final class MediaType extends ExtensibleObject {

    private Schema schema;
    private Schema itemSchema;
    private DocumentValue example;
    private Map<String, ReferenceOr<Example>> examples = new LinkedHashMap<>();
    private Map<String, Encoding> encoding = new LinkedHashMap<>();
    private List<Encoding> prefixEncoding = new ArrayList<>();
    private Encoding itemEncoding;
}
