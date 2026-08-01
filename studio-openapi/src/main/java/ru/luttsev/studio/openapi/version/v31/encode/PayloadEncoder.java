package ru.luttsev.studio.openapi.version.v31.encode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.media.Example;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Header;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class PayloadEncoder {

    private final SchemaEncoder schemaEncoder;
    private final ReferenceOrEncoder referenceOrEncoder =
            new ReferenceOrEncoder();
    private final ExampleEncoder exampleEncoder = new ExampleEncoder();
    private final MediaTypeEncoder mediaTypeEncoder = new MediaTypeEncoder();
    private final EncodingEncoder encodingEncoder = new EncodingEncoder();
    private final ParameterEncoder parameterEncoder = new ParameterEncoder();
    private final HeaderEncoder headerEncoder = new HeaderEncoder();
    private final RequestBodyEncoder requestBodyEncoder =
            new RequestBodyEncoder();
    private final ApiResponseEncoder apiResponseEncoder =
            new ApiResponseEncoder();
    private final LinkEncoder linkEncoder = new LinkEncoder();

    PayloadEncoder(SchemaEncoder schemaEncoder) {
        this.schemaEncoder = Objects.requireNonNull(
                schemaEncoder,
                "schemaEncoder must not be null");
    }

    DocumentValue encodeSchema(
            Schema source,
            EncodeContext context) {
        return schemaEncoder.encode(source, context);
    }

    ObjectValue encodeExamples(
            Map<String, ReferenceOr<Example>> source,
            EncodeContext context) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                exampleEncoder::encode);
    }

    ObjectValue encodeHeaders(
            Map<String, ReferenceOr<Header>> source,
            EncodeContext context) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                (header, childContext) ->
                        headerEncoder.encode(header, childContext, this));
    }

    ObjectValue encodeParameters(
            Map<String, ReferenceOr<Parameter>> source,
            EncodeContext context) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                (parameter, childContext) ->
                        parameterEncoder.encode(parameter, childContext, this));
    }

    ArrayValue encodeParameterList(
            List<ReferenceOr<Parameter>> source,
            EncodeContext context) {
        return ArrayValueEncoder.encodeObjects(
                source,
                context,
                (parameter, itemContext) -> referenceOrEncoder.encode(
                        parameter,
                        itemContext,
                        (value, childContext) -> parameterEncoder.encode(
                                value,
                                childContext,
                                this)));
    }

    ObjectValue encodeRequestBodies(
            Map<String, ReferenceOr<RequestBody>> source,
            EncodeContext context) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                (requestBody, childContext) ->
                        requestBodyEncoder.encode(
                                requestBody,
                                childContext,
                                this));
    }

    ObjectValue encodeRequestBody(
            ReferenceOr<RequestBody> source,
            EncodeContext context) {
        return referenceOrEncoder.encode(
                source,
                context,
                (requestBody, childContext) ->
                        requestBodyEncoder.encode(
                                requestBody,
                                childContext,
                                this));
    }

    ObjectValue encodeResponses(
            Map<String, ReferenceOr<ApiResponse>> source,
            EncodeContext context) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                (response, childContext) ->
                        apiResponseEncoder.encode(
                                response,
                                childContext,
                                this));
    }

    ObjectValue encodeResponse(
            ReferenceOr<ApiResponse> source,
            EncodeContext context) {
        return referenceOrEncoder.encode(
                source,
                context,
                (response, childContext) ->
                        apiResponseEncoder.encode(
                                response,
                                childContext,
                                this));
    }

    ObjectValue encodeLinks(
            Map<String, ReferenceOr<Link>> source,
            EncodeContext context) {
        return referenceOrEncoder.encodeMap(
                source,
                context,
                linkEncoder::encode);
    }

    ObjectValue encodeContent(
            Map<MediaTypeName, ReferenceOr<MediaType>> source,
            EncodeContext context) {
        LinkedHashMap<String, DocumentValue> values = new LinkedHashMap<>();
        if (source == null) {
            return new ObjectValue(values);
        }

        for (Map.Entry<MediaTypeName, ReferenceOr<MediaType>> entry
                : source.entrySet()) {
            String mediaTypeName = entry.getKey().value();
            EncodeContext itemContext = context.child(mediaTypeName);
            ReferenceOr<MediaType> mediaType = entry.getValue();
            if (mediaType instanceof ReferenceObject<?>) {
                itemContext.error(
                        OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_REFERENCE,
                        "Media Type references are not supported by OpenAPI "
                                + context.targetVersion().value());
                continue;
            }

            InlineObject<?> inlineObject = (InlineObject<?>) mediaType;
            MediaType inlineMediaType = (MediaType) inlineObject.value();
            values.put(
                    mediaTypeName,
                    mediaTypeEncoder.encode(
                            inlineMediaType,
                            itemContext,
                            this));
        }
        return new ObjectValue(values);
    }

    ObjectValue encodeEncodings(
            Map<String, Encoding> source,
            EncodeContext context) {
        return ObjectValueEncoder.encodeObjects(
                source,
                context,
                (encoding, itemContext) -> encodingEncoder.encode(
                        encoding,
                        itemContext,
                        this));
    }
}
