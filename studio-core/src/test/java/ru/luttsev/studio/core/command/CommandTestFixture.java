package ru.luttsev.studio.core.command;

import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;

public final class CommandTestFixture {

    private CommandTestFixture() {
    }

    public static OpenApiDocument document() {
        return new OpenApiDocumentFactory().create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Test API",
                "1.0.0"));
    }

    public static SchemaDefinition referenceTo(String reference) {
        SchemaDefinition schema = new SchemaDefinition();
        schema.setRef(new UriReference(reference));
        return schema;
    }
}
