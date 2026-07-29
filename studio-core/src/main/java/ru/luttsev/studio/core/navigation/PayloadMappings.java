package ru.luttsev.studio.core.navigation;

import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.response.ApiResponse;

final class PayloadMappings {

    private PayloadMappings() {
    }

    static void register(DocumentNodeRegistry registry) {
        registry.register(Parameter.class, PayloadMappings::collectParameter);
        registry.register(Header.class, PayloadMappings::collectHeader);
        registry.register(RequestBody.class, PayloadMappings::collectRequestBody);
        registry.register(ApiResponse.class, PayloadMappings::collectResponse);
        registry.register(MediaType.class, PayloadMappings::collectMediaType);
        registry.register(Example.class, PayloadMappings::collectExample);
        registry.register(Encoding.class, PayloadMappings::collectEncoding);
    }

    private static void collectParameter(
            Parameter parameter,
            ChildrenCollector children) {
        children.add("name", parameter.getName());
        children.add("in", parameter.getLocation());
        children.add("description", parameter.getDescription());
        children.add("required", parameter.getRequired());
        children.add("deprecated", parameter.getDeprecated());
        children.add("allowEmptyValue", parameter.getAllowEmptyValue());
        children.add("style", parameter.getStyle());
        children.add("explode", parameter.getExplode());
        children.add("allowReserved", parameter.getAllowReserved());
        children.add("schema", parameter.getSchema());
        children.add("example", parameter.getExample());
        children.add("examples", parameter.getExamples());
        children.add("content", parameter.getContent());
    }

    private static void collectHeader(Header header, ChildrenCollector children) {
        children.add("description", header.getDescription());
        children.add("required", header.getRequired());
        children.add("deprecated", header.getDeprecated());
        children.add("style", header.getStyle());
        children.add("explode", header.getExplode());
        children.add("schema", header.getSchema());
        children.add("example", header.getExample());
        children.add("examples", header.getExamples());
        children.add("content", header.getContent());
    }

    private static void collectRequestBody(
            RequestBody requestBody,
            ChildrenCollector children) {
        children.add("description", requestBody.getDescription());
        children.add("content", requestBody.getContent());
        children.add("required", requestBody.getRequired());
    }

    private static void collectResponse(
            ApiResponse response,
            ChildrenCollector children) {
        children.add("description", response.getDescription());
        children.add("headers", response.getHeaders());
        children.add("content", response.getContent());
        children.add("links", response.getLinks());
    }

    private static void collectMediaType(
            MediaType mediaType,
            ChildrenCollector children) {
        children.add("schema", mediaType.getSchema());
        children.add("itemSchema", mediaType.getItemSchema());
        children.add("example", mediaType.getExample());
        children.add("examples", mediaType.getExamples());
        children.add("encoding", mediaType.getEncoding());
        children.add("prefixEncoding", mediaType.getPrefixEncoding());
        children.add("itemEncoding", mediaType.getItemEncoding());
    }

    private static void collectExample(
            Example example,
            ChildrenCollector children) {
        children.add("summary", example.getSummary());
        children.add("description", example.getDescription());
        children.add("dataValue", example.getDataValue());
        children.add("serializedValue", example.getSerializedValue());
        children.add("externalValue", example.getExternalValue());
        children.add("value", example.getValue());
    }

    private static void collectEncoding(
            Encoding encoding,
            ChildrenCollector children) {
        children.add("contentType", encoding.getContentType());
        children.add("headers", encoding.getHeaders());
        children.add("style", encoding.getStyle());
        children.add("explode", encoding.getExplode());
        children.add("allowReserved", encoding.getAllowReserved());
        children.add("encoding", encoding.getEncoding());
        children.add("prefixEncoding", encoding.getPrefixEncoding());
        children.add("itemEncoding", encoding.getItemEncoding());
    }
}
