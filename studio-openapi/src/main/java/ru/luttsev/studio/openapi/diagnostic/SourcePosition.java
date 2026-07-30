package ru.luttsev.studio.openapi.diagnostic;

public record SourcePosition(int line, int column) {

    public SourcePosition {
        if (line < 1) {
            throw new IllegalArgumentException("line must be greater than zero");
        }
        if (column < 1) {
            throw new IllegalArgumentException("column must be greater than zero");
        }
    }
}
