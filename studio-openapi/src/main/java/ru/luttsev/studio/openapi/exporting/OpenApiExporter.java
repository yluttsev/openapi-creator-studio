package ru.luttsev.studio.openapi.exporting;

import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.openapi.result.ExportResult;

@FunctionalInterface
public interface OpenApiExporter {

    ExportResult exportDocument(
            OpenApiDocument document,
            ExportOptions options);
}
