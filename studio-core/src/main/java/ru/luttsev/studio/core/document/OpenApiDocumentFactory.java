package ru.luttsev.studio.core.document;

import java.util.Objects;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.path.Paths;

public final class OpenApiDocumentFactory {

    public OpenApiDocument create(NewDocumentParameters parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");

        var info = new Info();
        info.setTitle(parameters.title());
        info.setVersion(parameters.apiVersion());

        var document = new OpenApiDocument();
        document.setOpenApiVersion(parameters.openApiVersion());
        document.setInfo(info);
        document.setPaths(new Paths());
        document.setComponents(new Components());
        return document;
    }
}
