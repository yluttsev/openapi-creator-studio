package ru.luttsev.studio.openapi.version.v31.encode;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;

final class ObjectValueBuilder {

    private final Map<String, DocumentValue> fields = new LinkedHashMap<>();

    ObjectValueBuilder put(String field, DocumentValue value) {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(value, "value must not be null");
        if (fields.putIfAbsent(field, value) != null) {
            throw new IllegalStateException(
                    "Field '" + field + "' has already been written");
        }
        return this;
    }

    ObjectValueBuilder putString(String field, String value) {
        return value == null
                ? this
                : put(field, new StringValue(value));
    }

    ObjectValueBuilder putBoolean(String field, Boolean value) {
        return value == null
                ? this
                : put(field, new BooleanValue(value));
    }

    ObjectValueBuilder putNumber(String field, BigDecimal value) {
        return value == null
                ? this
                : put(field, new NumberValue(value));
    }

    ObjectValue build() {
        return new ObjectValue(fields);
    }
}
