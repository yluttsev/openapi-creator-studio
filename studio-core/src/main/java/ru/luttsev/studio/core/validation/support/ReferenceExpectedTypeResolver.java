package ru.luttsev.studio.core.validation.support;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class ReferenceExpectedTypeResolver {

    private static final Map<Class<?>, Map<String, Class<?>>> TARGET_TYPES = Map.ofEntries(
            Map.entry(Components.class, Map.ofEntries(
                    Map.entry("responses", ApiResponse.class),
                    Map.entry("parameters", Parameter.class),
                    Map.entry("examples", Example.class),
                    Map.entry("requestBodies", RequestBody.class),
                    Map.entry("headers", Header.class),
                    Map.entry("securitySchemes", SecurityScheme.class),
                    Map.entry("links", Link.class),
                    Map.entry("callbacks", Callback.class),
                    Map.entry("mediaTypes", MediaType.class))),
            Map.entry(PathItem.class, Map.of(
                    "parameters", Parameter.class)),
            Map.entry(Operation.class, Map.ofEntries(
                    Map.entry("parameters", Parameter.class),
                    Map.entry("requestBody", RequestBody.class),
                    Map.entry("responses", ApiResponse.class),
                    Map.entry("callbacks", Callback.class))),
            Map.entry(Parameter.class, Map.ofEntries(
                    Map.entry("examples", Example.class),
                    Map.entry("content", MediaType.class))),
            Map.entry(Header.class, Map.ofEntries(
                    Map.entry("examples", Example.class),
                    Map.entry("content", MediaType.class))),
            Map.entry(RequestBody.class, Map.of(
                    "content", MediaType.class)),
            Map.entry(ApiResponse.class, Map.ofEntries(
                    Map.entry("headers", Header.class),
                    Map.entry("content", MediaType.class),
                    Map.entry("links", Link.class))),
            Map.entry(MediaType.class, Map.of(
                    "examples", Example.class)),
            Map.entry(Encoding.class, Map.of(
                    "headers", Header.class)));

    private ReferenceExpectedTypeResolver() {
    }

    public static Optional<Class<?>> resolve(
            ValidationContext context,
            DocumentPath referencePath) {
        List<String> referenceSegments = referencePath.segments();
        Optional<DocumentPath> currentPath = referencePath.parent();
        while (currentPath.isPresent()) {
            DocumentPath ownerPath = currentPath.orElseThrow();
            Optional<Object> owner = context.navigator()
                    .find(context.document(), ownerPath);
            if (owner.isPresent()) {
                Map<String, Class<?>> fields =
                        TARGET_TYPES.get(owner.orElseThrow().getClass());
                int ownerDepth = ownerPath.segments().size();
                if (fields != null && ownerDepth < referenceSegments.size()) {
                    String field = referenceSegments.get(ownerDepth);
                    Class<?> expectedType = fields.get(field);
                    if (expectedType != null) {
                        return Optional.of(expectedType);
                    }
                }
            }
            currentPath = ownerPath.parent();
        }
        return Optional.empty();
    }
}
