package ru.luttsev.studio.openapi.importing;

import ru.luttsev.studio.openapi.result.ImportResult;

@FunctionalInterface
public interface OpenApiImporter {

    ImportResult importDocument(String content, ImportOptions options);
}
