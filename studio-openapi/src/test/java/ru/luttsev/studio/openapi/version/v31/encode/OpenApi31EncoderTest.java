package ru.luttsev.studio.openapi.version.v31.encode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.info.Info;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.tag.Tag;
import ru.luttsev.studio.core.model.value.ObjectValue;
import ru.luttsev.studio.core.model.value.StringValue;
import ru.luttsev.studio.openapi.diagnostic.DiagnosticPhase;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnostic;
import ru.luttsev.studio.openapi.diagnostic.OpenApiDiagnosticCodes;
import ru.luttsev.studio.openapi.importing.ImportOptions;
import ru.luttsev.studio.openapi.result.AdapterFailure;
import ru.luttsev.studio.openapi.result.AdapterSuccess;
import ru.luttsev.studio.openapi.result.SyntaxSuccess;
import ru.luttsev.studio.openapi.syntax.JacksonOpenApiSyntaxCodec;
import ru.luttsev.studio.openapi.syntax.ParsedDocument;
import ru.luttsev.studio.openapi.testing.TestResources;
import ru.luttsev.studio.openapi.version.v31.decode.OpenApi31Decoder;

class OpenApi31EncoderTest {

    private final OpenApi31Encoder encoder = new OpenApi31Encoder();

    @Test
    void roundTripsCompleteOpenApi31Document() {
        ObjectValue source = parse(TestResources.readFixture(
                "v31/encode/complete-document-round-trip.yaml"));
        OpenApiDocument document = decode(source);

        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                encoder.encode(document, OpenApiVersion.V3_1_2));

        assertTrue(success.diagnostics().isEmpty());
        assertEquals(source, success.value());
    }

    @Test
    void writesSelectedTargetPatchVersion() {
        OpenApiDocument document = new OpenApiDocument();
        document.setOpenApiVersion(new OpenApiVersion("3.1.0"));
        Info info = new Info();
        info.setTitle("API");
        info.setVersion("1.0.0");
        document.setInfo(info);

        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                encoder.encode(document, OpenApiVersion.V3_1_2));
        ObjectValue encoded = assertInstanceOf(
                ObjectValue.class,
                success.value());

        assertEquals(
                new StringValue("3.1.2"),
                encoded.values().get("openapi"));
    }

    @Test
    void reportsOpenApi32RootAndTagFields() {
        OpenApiDocument document = new OpenApiDocument();
        document.setSelf(new UriReference("https://example.com/openapi.yaml"));
        Tag tag = new Tag();
        tag.setName("events");
        tag.setSummary("Event management");
        tag.setParent("platform");
        tag.setKind("nav");
        document.setTags(List.of(tag));

        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                encoder.encode(document, OpenApiVersion.V3_1_2));
        Set<String> paths = failure.diagnostics().stream()
                .map(OpenApiDiagnostic::path)
                .map(path -> path.toPointer())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "/self",
                "/tags/0/summary",
                "/tags/0/parent",
                "/tags/0/kind"), paths);
        assertTrue(failure.diagnostics().stream().allMatch(diagnostic ->
                diagnostic.code().equals(
                        OpenApiDiagnosticCodes.VERSION_UNSUPPORTED_FIELD)
                        && diagnostic.phase()
                                == DiagnosticPhase.VERSION_COMPATIBILITY));
    }

    @Test
    void rejectsUnsupportedTargetVersionFamily() {
        AdapterFailure<?> failure = assertInstanceOf(
                AdapterFailure.class,
                encoder.encode(
                        new OpenApiDocument(),
                        OpenApiVersion.V3_0_4));

        assertEquals(1, failure.diagnostics().size());
        OpenApiDiagnostic diagnostic = failure.diagnostics().getFirst();
        assertEquals(
                OpenApiDiagnosticCodes.UNSUPPORTED_MAPPING_VERSION,
                diagnostic.code());
        assertEquals(
                DiagnosticPhase.VERSION_COMPATIBILITY,
                diagnostic.phase());
        assertEquals("/openapi", diagnostic.path().toPointer());
    }

    private static OpenApiDocument decode(ObjectValue source) {
        AdapterSuccess<?> success = assertInstanceOf(
                AdapterSuccess.class,
                new OpenApi31Decoder().decode(
                        source,
                        OpenApiVersion.V3_1_2));
        assertTrue(success.diagnostics().isEmpty());
        return assertInstanceOf(OpenApiDocument.class, success.value());
    }

    private static ObjectValue parse(String content) {
        JacksonOpenApiSyntaxCodec codec = new JacksonOpenApiSyntaxCodec();
        SyntaxSuccess<?> success = assertInstanceOf(
                SyntaxSuccess.class,
                codec.parse(content, ImportOptions.autoDetect()));
        ParsedDocument parsedDocument = assertInstanceOf(
                ParsedDocument.class,
                success.value());
        return parsedDocument.root();
    }
}
