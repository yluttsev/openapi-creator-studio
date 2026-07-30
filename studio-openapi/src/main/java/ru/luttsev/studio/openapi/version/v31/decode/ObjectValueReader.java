package ru.luttsev.studio.openapi.version.v31.decode;

import java.math.BigDecimal;
import java.util.Objects;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.BooleanValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.NullValue;
import ru.luttsev.studio.core.model.value.NumberValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class ObjectValueReader {

    private final ObjectValue source;
    private final DecodeContext context;

    ObjectValueReader(ObjectValue source, DecodeContext context) {
        this.source = Objects.requireNonNull(
                source,
                "source must not be null");
        this.context = Objects.requireNonNull(
                context,
                "context must not be null");
    }

    String requiredString(String field) {
        DocumentValue value = requiredValue(field);
        return value == null ? null : asString(field, value);
    }

    String optionalString(String field) {
        DocumentValue value = source.values().get(field);
        return value == null ? null : asString(field, value);
    }

    UriReference optionalUriReference(String field) {
        String value = optionalString(field);
        return value == null ? null : new UriReference(value);
    }

    Boolean optionalBoolean(String field) {
        DocumentValue value = source.values().get(field);
        if (value == null) {
            return null;
        }
        if (value instanceof BooleanValue booleanValue) {
            return booleanValue.value();
        }
        typeMismatch(field, "boolean", value);
        return null;
    }

    BigDecimal optionalNumber(String field) {
        DocumentValue value = source.values().get(field);
        if (value == null) {
            return null;
        }
        if (value instanceof NumberValue numberValue) {
            return numberValue.value();
        }
        typeMismatch(field, "number", value);
        return null;
    }

    ObjectValue requiredObject(String field) {
        DocumentValue value = requiredValue(field);
        return value == null ? null : asObject(field, value);
    }

    ObjectValue optionalObject(String field) {
        DocumentValue value = source.values().get(field);
        return value == null ? null : asObject(field, value);
    }

    ArrayValue optionalArray(String field) {
        DocumentValue value = source.values().get(field);
        if (value == null) {
            return null;
        }
        if (value instanceof ArrayValue arrayValue) {
            return arrayValue;
        }
        typeMismatch(field, "array", value);
        return null;
    }

    DocumentValue optionalValue(String field) {
        return source.values().get(field);
    }

    private DocumentValue requiredValue(String field) {
        DocumentValue value = source.values().get(field);
        if (value == null) {
            context.child(field).error(
                    OpenApiDiagnosticCodes.MAPPING_MISSING_REQUIRED_FIELD,
                    "Required field '" + field + "' is missing");
        }
        return value;
    }

    private String asString(String field, DocumentValue value) {
        if (value instanceof StringValue stringValue) {
            return stringValue.value();
        }
        typeMismatch(field, "string", value);
        return null;
    }

    private ObjectValue asObject(String field, DocumentValue value) {
        if (value instanceof ObjectValue objectValue) {
            return objectValue;
        }
        typeMismatch(field, "object", value);
        return null;
    }

    private void typeMismatch(
            String field,
            String expectedType,
            DocumentValue value) {
        context.child(field).error(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "Expected " + expectedType + " but found " + typeOf(value));
    }

    private static String typeOf(DocumentValue value) {
        return switch (value) {
            case ArrayValue ignored -> "array";
            case BooleanValue ignored -> "boolean";
            case NullValue ignored -> "null";
            case NumberValue ignored -> "number";
            case ObjectValue ignored -> "object";
            case StringValue ignored -> "string";
        };
    }
}
