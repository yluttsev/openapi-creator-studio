package ru.luttsev.studio.core.navigation;

import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.path.Paths;
import ru.luttsev.studio.core.model.response.Responses;

final class PathMappings {

    private PathMappings() {
    }

    static void register(DocumentNodeRegistry registry) {
        registry.register(Paths.class, PathMappings::collectPaths);
        registry.register(PathItem.class, PathMappings::collectPathItem);
        registry.register(Operation.class, PathMappings::collectOperation);
        registry.register(Responses.class, PathMappings::collectResponses);
        registry.register(Callback.class, PathMappings::collectCallback);
        registry.register(Link.class, PathMappings::collectLink);
    }

    private static void collectPaths(Paths paths, ChildrenCollector children) {
        children.addEntries(paths.getItems());
    }

    private static void collectPathItem(
            PathItem pathItem,
            ChildrenCollector children) {
        children.add("$ref", pathItem.getRef());
        children.add("summary", pathItem.getSummary());
        children.add("description", pathItem.getDescription());
        children.addOperations(pathItem.getOperations());
        children.add("servers", pathItem.getServers());
        children.add("parameters", pathItem.getParameters());
    }

    private static void collectOperation(
            Operation operation,
            ChildrenCollector children) {
        children.add("tags", operation.getTags());
        children.add("summary", operation.getSummary());
        children.add("description", operation.getDescription());
        children.add("externalDocs", operation.getExternalDocs());
        children.add("operationId", operation.getOperationId());
        children.add("parameters", operation.getParameters());
        children.add("requestBody", operation.getRequestBody());
        children.add("responses", operation.getResponses());
        children.add("callbacks", operation.getCallbacks());
        children.add("deprecated", operation.getDeprecated());
        children.add("security", operation.getSecurity());
        children.add("servers", operation.getServers());
    }

    private static void collectResponses(
            Responses responses,
            ChildrenCollector children) {
        children.addEntries(responses.getValues());
    }

    private static void collectCallback(
            Callback callback,
            ChildrenCollector children) {
        children.addEntries(callback.getExpressions());
    }

    private static void collectLink(Link link, ChildrenCollector children) {
        children.add("operationRef", link.getOperationRef());
        children.add("operationId", link.getOperationId());
        children.add("parameters", link.getParameters());
        children.add("requestBody", link.getRequestBody());
        children.add("description", link.getDescription());
        children.add("server", link.getServer());
    }
}
