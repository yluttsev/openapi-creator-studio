package ru.luttsev.studio.core.model.response;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

public final class Responses extends ExtensibleObject {

    private final Map<ResponseKey, ReferenceOr<ApiResponse>> values = new LinkedHashMap<>();

    public Map<ResponseKey, ReferenceOr<ApiResponse>> getValues() {
        return this.values;
    }
}
