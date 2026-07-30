package ru.luttsev.studio.openapi.syntax;

import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.SyntaxResult;

@FunctionalInterface
public interface OpenApiSyntaxParser {

    SyntaxResult<ParsedDocument> parse(
            String content,
            ImportOptions options);
}
