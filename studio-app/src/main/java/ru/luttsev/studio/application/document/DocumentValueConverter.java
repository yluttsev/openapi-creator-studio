package ru.luttsev.studio.application.document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NullValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;

@Component
public final class DocumentValueConverter {

    public Map<String, Object> toMap(ObjectValue value) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        value.values().forEach((key, child) -> result.put(key, convert(child)));
        return result;
    }

    private Object convert(DocumentValue value) {
        if (value instanceof ObjectValue objectValue) {
            return toMap(objectValue);
        }
        if (value instanceof ArrayValue arrayValue) {
            List<Object> result = new ArrayList<>(arrayValue.values().size());
            for (DocumentValue child : arrayValue.values()) {
                result.add(convert(child));
            }
            return result;
        }
        if (value instanceof StringValue stringValue) {
            return stringValue.value();
        }
        if (value instanceof NumberValue numberValue) {
            return numberValue.value();
        }
        if (value instanceof BooleanValue booleanValue) {
            return booleanValue.value();
        }
        if (value == NullValue.INSTANCE) {
            return null;
        }
        throw new IllegalArgumentException(
                "Unsupported document value: " + value.getClass().getName());
    }
}
