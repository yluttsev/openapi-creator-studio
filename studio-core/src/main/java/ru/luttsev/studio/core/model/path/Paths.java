package ru.luttsev.studio.core.model.path;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import ru.luttsev.studio.core.model.ExtensibleObject;

@Getter
public final class Paths extends ExtensibleObject {

    private final Map<String, PathItem> items = new LinkedHashMap<>();
}
