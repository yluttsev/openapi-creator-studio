package ru.luttsev.studio.core.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.callback.Callback;
import ru.luttsev.studio.core.model.link.Link;
import ru.luttsev.studio.core.model.media.Encoding;
import ru.luttsev.studio.core.model.media.MediaType;
import ru.luttsev.studio.core.model.media.MediaTypeName;
import ru.luttsev.studio.core.model.media.RequestBody;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.response.ApiResponse;
import ru.luttsev.studio.core.model.response.ResponseKey;
import ru.luttsev.studio.core.model.schema.Discriminator;
import ru.luttsev.studio.core.model.schema.JsonType;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.value.StringValue;

class PayloadValidationRulesTest {

    @Test
    void validatesEncodingContextMediaTypeAndSchemaProperties() {
        OpenApiDocument document = ValidationFixture.document();
        Operation operation = ValidationFixture.addOperation(
                document,
                "/users",
                HttpMethod.POST,
                "createUser");
        SchemaDefinition schema = new SchemaDefinition();
        schema.setTypes(Set.of(JsonType.OBJECT));
        schema.getProperties().put("id", new SchemaDefinition());
        MediaType mediaType = new MediaType();
        mediaType.setSchema(schema);
        mediaType.getEncoding().put("name", new Encoding());
        RequestBody requestBody = new RequestBody();
        requestBody.getContent().put(
                MediaTypeName.APPLICATION_JSON,
                ValidationFixture.inline(mediaType));
        operation.setRequestBody(ValidationFixture.inline(requestBody));

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("encoding.media-type.invalid"));
        assertTrue(codes.contains("encoding.property.unknown"));
    }

    @Test
    void validatesDiscriminatorCompositionMappingAndRequiredProperty() {
        OpenApiDocument document = ValidationFixture.document();
        SchemaDefinition schema = new SchemaDefinition();
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("kind");
        discriminator.getMapping().put("cat", "MissingCat");
        schema.setDiscriminator(discriminator);
        document.getComponents().getSchemas().put("Pet", schema);

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("discriminator.composition.missing"));
        assertTrue(codes.contains("discriminator.mapping.target.not-found"));
        assertTrue(codes.contains("discriminator.property.not-required"));
    }

    @Test
    void validatesLinkTargetsParametersAndRuntimeExpressions() {
        OpenApiDocument document = ValidationFixture.document();
        Operation source = ValidationFixture.addOperation(
                document,
                "/orders",
                HttpMethod.POST,
                "createOrder");
        Operation target = ValidationFixture.addOperation(
                document,
                "/users/{id}",
                HttpMethod.GET,
                "findUser");
        Parameter id = new Parameter();
        id.setName("id");
        id.setLocation(ParameterLocation.PATH);
        id.setRequired(true);
        target.getParameters().add(ValidationFixture.inline(id));

        Link link = new Link();
        link.setOperationId("findUser");
        link.getParameters().put(
                "path.missing",
                new StringValue("$request.query.unknown"));
        link.setRequestBody(new StringValue("$response.header.Location"));
        ApiResponse response = new ApiResponse();
        response.getLinks().put("user", ValidationFixture.inline(link));
        source.getResponses().put(
                new ResponseKey("201"),
                ValidationFixture.inline(response));

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("link.target-parameter.unknown"));
        assertTrue(codes.contains("link.expression.request-parameter.unknown"));
        assertTrue(codes.contains("link.expression.response-header.unknown"));
    }

    @Test
    void validatesCallbackSyntaxAndParentOperationParameters() {
        OpenApiDocument document = ValidationFixture.document();
        Operation source = ValidationFixture.addOperation(
                document,
                "/subscriptions",
                HttpMethod.POST,
                "subscribe");
        Callback callback = new Callback();
        callback.getExpressions().put(
                "{$request.query.callbackUrl}",
                new PathItem());
        callback.getExpressions().put("invalid", new PathItem());
        source.getCallbacks().put(
                "notification",
                ValidationFixture.inline(callback));

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("callback.expression.invalid"));
        assertTrue(codes.contains("callback.expression.parameter.unknown"));
    }
}
