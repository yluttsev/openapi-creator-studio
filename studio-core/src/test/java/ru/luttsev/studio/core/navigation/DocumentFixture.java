package ru.luttsev.studio.core.navigation;

import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;

record DocumentFixture(
        OpenApiDocument document,
        Operation operation,
        SchemaDefinition nameSchema) {
}
