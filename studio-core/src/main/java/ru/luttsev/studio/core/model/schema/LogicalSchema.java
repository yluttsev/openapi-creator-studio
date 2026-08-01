package ru.luttsev.studio.core.model.schema;

public final class LogicalSchema implements Schema {

    private boolean value;

    public boolean isValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public LogicalSchema() {
    }

    public LogicalSchema(boolean value) {
        this.value = value;
    }
}
