package ru.luttsev.studio.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import ru.luttsev.studio.core.model.value.DocumentValue;

@Getter
public abstract class ExtensibleObject {

    private final Map<String, DocumentValue> additionalFields = new LinkedHashMap<>();
}
