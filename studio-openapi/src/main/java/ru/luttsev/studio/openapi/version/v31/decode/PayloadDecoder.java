package ru.luttsev.studio.openapi.version.v31.decode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
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
        return decodeReferenceMap(
                source,
                context,
                this::decodeExampleOrReference);
    }

    Map<String, ReferenceOr<Header>> decodeHeaders(
            ObjectValue source,
            DecodeContext context) {
        return decodeReferenceMap(
                source,
                context,
                this::decodeHeaderOrReference);
    }

    Map<String, ReferenceOr<Parameter>> decodeParameters(
            ObjectValue source,
            DecodeContext context) {
        return decodeReferenceMap(
                source,
                context,
                this::decodeParameterOrReference);
    }

    Map<String, ReferenceOr<RequestBody>> decodeRequestBodies(
            ObjectValue source,
            DecodeContext context) {
        return decodeReferenceMap(
                source,
                context,
                this::decodeRequestBodyOrReference);
    }

    Map<String, ReferenceOr<ApiResponse>> decodeResponses(
            ObjectValue source,
            DecodeContext context) {
        return decodeReferenceMap(
                source,
                context,
                this::decodeResponseOrReference);
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

    private ReferenceOr<Example> decodeExampleOrReference(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                exampleDecoder::decode);
    }

    private ReferenceOr<Header> decodeHeaderOrReference(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                (object, childContext) ->
                        headerDecoder.decode(object, childContext, this));
    }

    private ReferenceOr<Parameter> decodeParameterOrReference(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                (object, childContext) ->
                        parameterDecoder.decode(object, childContext, this));
    }

    private ReferenceOr<RequestBody> decodeRequestBodyOrReference(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                (object, childContext) ->
                        requestBodyDecoder.decode(object, childContext, this));
    }

    private ReferenceOr<ApiResponse> decodeResponseOrReference(
            DocumentValue source,
            DecodeContext context) {
        return referenceOrDecoder.decode(
                source,
                context,
                (object, childContext) ->
                        apiResponseDecoder.decode(object, childContext, this));
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

    private static <T> Map<String, ReferenceOr<T>> decodeReferenceMap(
            ObjectValue source,
            DecodeContext context,
            BiFunction<DocumentValue, DecodeContext, ReferenceOr<T>> decoder) {
        LinkedHashMap<String, ReferenceOr<T>> values = new LinkedHashMap<>();
        if (source == null) {
            return values;
        }

        for (Map.Entry<String, DocumentValue> entry : source.values().entrySet()) {
            ReferenceOr<T> value = decoder.apply(
                    entry.getValue(),
                    context.child(entry.getKey()));
            if (value != null) {
                values.put(entry.getKey(), value);
            }
        }
        return values;
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
