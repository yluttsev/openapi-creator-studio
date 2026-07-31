package ru.luttsev.studio.core.model.response;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import ru.luttsev.studio.core.model.ExtensibleObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;

@Getter
public final class Responses extends ExtensibleObject {

    private final Map<ResponseKey, ReferenceOr<ApiResponse>> values = new LinkedHashMap<>();
}
