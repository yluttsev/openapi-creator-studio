package ru.luttsev.studio.core.model.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
@Setter
@NoArgsConstructor
public final class Discriminator {

    private String propertyName;
    private Map<String, String> mapping = new LinkedHashMap<>();
    private String defaultMapping;
    private Map<String, DocumentValue> extensions = new LinkedHashMap<>();
}
