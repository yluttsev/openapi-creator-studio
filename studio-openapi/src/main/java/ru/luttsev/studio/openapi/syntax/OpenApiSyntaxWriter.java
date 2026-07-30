package ru.luttsev.studio.openapi.syntax;

import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.openapi.result.SyntaxResult;

@FunctionalInterface
public interface OpenApiSyntaxWriter {

    SyntaxResult<String> write(
            ObjectValue document,
            OpenApiFormat format);
}
