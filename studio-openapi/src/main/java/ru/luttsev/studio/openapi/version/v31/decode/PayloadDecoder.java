package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.ArrayList;
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
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.schema.Schema;
import ru.luttsev.studio.core.model.value.ArrayValue;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;

final class PayloadDecoder {

    private final SchemaDecoder schemaDecoder;
    private final ReferenceOrDecoder referenceOrDecoder =
            new ReferenceOrDecoder();
    private final ExampleDecoder exampleDecoder = new ExampleDecoder();
    private final MediaTypeDecoder mediaTypeDecoder = new MediaTypeDecoder();
    private final EncodingDecoder encodingDecoder = new EncodingDecoder();
    private final ParameterDecoder parameterDecoder = new ParameterDecoder();
    private final HeaderDecoder headerDecoder = new HeaderDecoder();
    private final RequestBodyDecoder requestBodyDecoder =
            new RequestBodyDecoder();
    private final ApiResponseDecoder apiResponseDecoder =
            new ApiResponseDecoder();
    private final LinkDecoder linkDecoder = new LinkDecoder();

    PayloadDecoder(SchemaDecoder schemaDecoder) {
        this.schemaDecoder = Objects.requireNonNull(
                schemaDecoder,
                "schemaDecoder must not be null");
    }

    Schema decodeSchema(
            DocumentValue source,
            DecodeContext context) {
        return schemaDecoder.decode(source, context);
    }

    Map<String, ReferenceOr<Example>> decodeExamples(
            ObjectValue source,
            DecodeContext context) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                exampleDecoder::decode);
    }

    Map<String, ReferenceOr<Header>> decodeHeaders(
            ObjectValue source,
            DecodeContext context) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                (object, childContext) ->
                        headerDecoder.decode(object, childContext, this));
    }

    Map<String, ReferenceOr<Parameter>> decodeParameters(
            ObjectValue source,
            DecodeContext context) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                (object, childContext) ->
                        parameterDecoder.decode(object, childContext, this));
    }

    List<ReferenceOr<Parameter>> decodeParameterList(
            ArrayValue source,
            DecodeContext context) {
        ArrayList<ReferenceOr<Parameter>> parameters = new ArrayList<>();
        if (source == null) {
            return parameters;
        }

        for (int index = 0; index < source.values().size(); index++) {
            ReferenceOr<Parameter> parameter = referenceOrDecoder.decode(
                    source.values().get(index),
                    context.child(Integer.toString(index)),
                    (object, childContext) ->
                            parameterDecoder.decode(object, childContext, this));
            if (parameter != null) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    Map<String, ReferenceOr<RequestBody>> decodeRequestBodies(
            ObjectValue source,
            DecodeContext context) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                (object, childContext) ->
                        requestBodyDecoder.decode(object, childContext, this));
    }

    ReferenceOr<RequestBody> decodeRequestBody(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                (object, childContext) ->
                        requestBodyDecoder.decode(object, childContext, this));
    }

    Map<String, ReferenceOr<ApiResponse>> decodeResponses(
            ObjectValue source,
            DecodeContext context) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                (object, childContext) ->
                        apiResponseDecoder.decode(object, childContext, this));
    }

    ReferenceOr<ApiResponse> decodeResponse(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                (object, childContext) ->
                        apiResponseDecoder.decode(object, childContext, this));
    }

    Map<String, ReferenceOr<Link>> decodeLinks(
            ObjectValue source,
            DecodeContext context) {
        return referenceOrDecoder.decodeMap(
                source,
                context,
                linkDecoder::decode);
    }

    Map<MediaTypeName, ReferenceOr<MediaType>> decodeContent(
            ObjectValue source,
            DecodeContext context) {
        LinkedHashMap<MediaTypeName, ReferenceOr<MediaType>> content =
                new LinkedHashMap<>();
        if (source == null) {
            return content;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            ReferenceOr<MediaType> mediaType = decodeInlineMediaType(
                    entry.getValue(),
                    context.child(entry.getKey()));
            if (mediaType != null) {
                content.put(new MediaTypeName(entry.getKey()), mediaType);
            }
        }
        return content;
    }

    Map<String, Encoding> decodeEncodings(
            ObjectValue source,
            DecodeContext context) {
        LinkedHashMap<String, Encoding> encodings = new LinkedHashMap<>();
        if (source == null) {
            return encodings;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            ObjectValue encodingSource = asObject(
                    entry.getValue(),
                    context.child(entry.getKey()));
            if (encodingSource != null) {
                encodings.put(
                        entry.getKey(),
                        encodingDecoder.decode(
                                encodingSource,
                                context.child(entry.getKey()),
                                this));
            }
        }
        return encodings;
    }

    private ReferenceOr<MediaType> decodeInlineMediaType(
            DocumentValue source,
            DecodeContext context) {
        ObjectValue object = asObject(source, context);
        if (object == null) {
            return null;
        }
        return new InlineObject<>(
                mediaTypeDecoder.decode(object, context, this));
    }

    private static ObjectValue asObject(
            DocumentValue source,
            DecodeContext context) {
        if (source instanceof ObjectValue objectValue) {
            return objectValue;
        }
        context.error(
                OpenApiDiagnosticCodes.MAPPING_TYPE_MISMATCH,
                "Expected object but found "
                        + ObjectValueReader.typeOf(source));
        return null;
    }
}
