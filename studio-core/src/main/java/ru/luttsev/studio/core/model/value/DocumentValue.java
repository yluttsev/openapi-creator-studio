package ru.luttsev.studio.core.model.value;

public sealed interface DocumentValue
        permits ArrayValue, BooleanValue, NullValue, NumberValue, ObjectValue, StringValue {
}
