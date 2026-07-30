package ru.luttsev.studio.openapi.syntax;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ru.luttsev.studio.core.model.value.DocumentValue;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticSeverity;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.diagnostic.SourcePosition;
import ru.luttsev.studio.openapi.format.OpenApiFormat;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.SyntaxFailure;
import ru.luttsev.studio.openapi.result.SyntaxResult;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

public final class JacksonOpenApiSyntaxCodec implements OpenApiSyntaxCodec {

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final JacksonDocumentValueMapper valueMapper;

    public JacksonOpenApiSyntaxCodec() {
        jsonMapper = JsonMapper.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        yamlMapper = YAMLMapper.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
                .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER)
                .disable(YAMLWriteFeature.SPLIT_LINES)
                .enable(YAMLWriteFeature.MINIMIZE_QUOTES)
                .enable(YAMLWriteFeature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
                .build();
        valueMapper = new JacksonDocumentValueMapper();
    }

    @Override
    public SyntaxResult<ParsedDocument> parse(
            String content,
            ImportOptions options) {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(options, "options must not be null");

        OpenApiFormat format = options.formatHint()
                .orElseGet(() -> OpenApiFormatDetector.detect(content));
        String normalizedContent =
                OpenApiFormatDetector.removeByteOrderMark(content);

        try {
            JsonNode rootNode = mapperFor(format).readTree(normalizedContent);
            if (rootNode == null || rootNode.isMissingNode()) {
                return failure(emptyDocumentDiagnostic());
            }

            DocumentValue rootValue = valueMapper.fromJsonNode(rootNode);
            if (!(rootValue instanceof ObjectValue objectValue)) {
                return failure(rootNotObjectDiagnostic());
            }
            return new SyntaxSuccess<>(
                    new ParsedDocument(objectValue, format),
                    List.of());
        } catch (JacksonException exception) {
            return failure(invalidSyntaxDiagnostic(format, exception));
        } catch (UnsupportedSyntaxValueException exception) {
            return failure(unsupportedValueDiagnostic(exception));
        }
    }

    @Override
    public SyntaxResult<String> write(
            ObjectValue document,
            OpenApiFormat format) {
        Objects.requireNonNull(document, "document must not be null");
        Objects.requireNonNull(format, "format must not be null");

        try {
            JsonNode rootNode = valueMapper.toJsonNode(document);
            String content = mapperFor(format).writeValueAsString(rootNode);
            return new SyntaxSuccess<>(content, List.of());
        } catch (JacksonException exception) {
            return failure(serializationDiagnostic(exception));
        }
    }

    private ObjectMapper mapperFor(OpenApiFormat format) {
        return switch (format) {
            case JSON -> jsonMapper;
            case YAML -> yamlMapper;
        };
    }

    private static <T> SyntaxFailure<T> failure(
            OpenApiDiagnostic diagnostic) {
        return new SyntaxFailure<>(List.of(diagnostic));
    }

    private static OpenApiDiagnostic emptyDocumentDiagnostic() {
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.EMPTY_DOCUMENT,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.PARSING,
                "The document is empty",
                DocumentPath.root());
    }

    private static OpenApiDiagnostic rootNotObjectDiagnostic() {
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.ROOT_NOT_OBJECT,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.PARSING,
                "The document root must be an object",
                DocumentPath.root());
    }

    private static OpenApiDiagnostic invalidSyntaxDiagnostic(
            OpenApiFormat format,
            JacksonException exception) {
        String originalMessage = exception.getOriginalMessage();
        String details = originalMessage == null || originalMessage.isBlank()
                ? "Unknown parser error"
                : originalMessage;
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.INVALID_SYNTAX,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.PARSING,
                "Invalid " + format + " syntax: " + details,
                DocumentPath.root(),
                sourcePosition(exception));
    }

    private static OpenApiDiagnostic unsupportedValueDiagnostic(
            UnsupportedSyntaxValueException exception) {
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.UNSUPPORTED_SYNTAX_VALUE,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.PARSING,
                exception.getMessage(),
                DocumentPath.root());
    }

    private static OpenApiDiagnostic serializationDiagnostic(
            JacksonException exception) {
        String originalMessage = exception.getOriginalMessage();
        String details = originalMessage == null || originalMessage.isBlank()
                ? "Unknown serializer error"
                : originalMessage;
        return new OpenApiDiagnostic(
                OpenApiDiagnosticCodes.SERIALIZATION_FAILED,
                DiagnosticSeverity.ERROR,
                DiagnosticPhase.SERIALIZATION,
                "Unable to serialize the document: " + details,
                DocumentPath.root());
    }

    private static Optional<SourcePosition> sourcePosition(
            JacksonException exception) {
        TokenStreamLocation location = exception.getLocation();
        if (location == null
                || location.getLineNr() < 1
                || location.getColumnNr() < 1) {
            return Optional.empty();
        }
        return Optional.of(new SourcePosition(
                location.getLineNr(),
                location.getColumnNr()));
    }
}
