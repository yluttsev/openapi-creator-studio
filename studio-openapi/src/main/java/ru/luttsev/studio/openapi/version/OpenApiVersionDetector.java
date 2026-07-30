package ru.luttsev.studio.openapi.version;

import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.result.VersionDetectionResult;

@FunctionalInterface
public interface OpenApiVersionDetector {

    VersionDetectionResult detect(ObjectValue document);
}
