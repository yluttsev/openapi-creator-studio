package ru.luttsev.studio.testsupport;

import ru.luttsev.studio.core.document.NewDocumentParameters;
import ru.luttsev.studio.core.document.OpenApiDocumentFactory;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;

public final class OpenApiDocuments {

    private static final OpenApiDocumentFactory FACTORY = new OpenApiDocumentFactory();

    private OpenApiDocuments() {
    }

    public static OpenApiDocument blank() {
        return FACTORY.create(new NewDocumentParameters(
                OpenApiVersion.V3_1_2,
                "Test API",
                "1.0.0"));
    }

    public static OpenApiDocument blankWithoutVersion() {
        OpenApiDocument document = blank();
        document.setOpenApiVersion(null);
        return document;
    }
}
