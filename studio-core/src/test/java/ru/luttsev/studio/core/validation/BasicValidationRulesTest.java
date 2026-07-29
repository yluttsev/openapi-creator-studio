package ru.luttsev.studio.core.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.parameter.ParameterLocation;
import ru.luttsev.studio.core.model.path.HttpMethod;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.server.Server;
import ru.luttsev.studio.core.model.server.ServerVariable;
import ru.luttsev.studio.core.model.tag.Tag;

class BasicValidationRulesTest {

    @Test
    void reportsDuplicateOperationIdsAcrossTheDocument() {
        OpenApiDocument document = ValidationFixture.document();
        ValidationFixture.addOperation(document, "/users", HttpMethod.GET, "find");
        ValidationFixture.addOperation(document, "/accounts", HttpMethod.GET, "find");

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("operation.operation-id.duplicate"));
    }

    @Test
    void validatesPathTemplatesAndParameterLists() {
        OpenApiDocument document = ValidationFixture.document();
        Operation operation =
                ValidationFixture.addOperation(
                        document,
                        "/users/{id}",
                        HttpMethod.GET,
                        "findUser");

        Parameter firstQuery = parameter("filter", ParameterLocation.QUERY, false);
        Parameter secondQuery = parameter("filter", ParameterLocation.QUERY, false);
        Parameter extraPath = parameter("other", ParameterLocation.PATH, false);
        operation.getParameters().add(ValidationFixture.inline(firstQuery));
        operation.getParameters().add(ValidationFixture.inline(secondQuery));
        operation.getParameters().add(ValidationFixture.inline(extraPath));

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("parameter.name-and-location.duplicate"));
        assertTrue(codes.contains("path.template.parameter.missing"));
        assertTrue(codes.contains("path.parameter.template.missing"));
        assertTrue(codes.contains("path.parameter.required"));
    }

    @Test
    void allowsOperationParametersToOverridePathItemParameters() {
        OpenApiDocument document = ValidationFixture.document();
        Operation operation =
                ValidationFixture.addOperation(
                        document,
                        "/users/{id}",
                        HttpMethod.GET,
                        "findUser");
        PathItem pathItem = document.getPaths().getItems().get("/users/{id}");
        pathItem.getParameters().add(ValidationFixture.inline(
                parameter("id", ParameterLocation.PATH, true)));
        operation.getParameters().add(ValidationFixture.inline(
                parameter("id", ParameterLocation.PATH, true)));

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertFalse(codes.contains("parameter.name-and-location.duplicate"));
        assertFalse(codes.contains("path.template.parameter.missing"));
    }

    @Test
    void reportsAmbiguousPathsAndDuplicateTags() {
        OpenApiDocument document = ValidationFixture.document();
        ValidationFixture.addOperation(
                document,
                "/users/{id}",
                HttpMethod.GET,
                "findById");
        ValidationFixture.addOperation(
                document,
                "/users/{name}",
                HttpMethod.GET,
                "findByName");
        Tag first = new Tag();
        first.setName("users");
        Tag second = new Tag();
        second.setName("users");
        document.setTags(List.of(first, second));

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("path.template.ambiguous"));
        assertTrue(codes.contains("tag.name.duplicate"));
    }

    @Test
    void validatesServerVariablesAndExpandedUrls() {
        OpenApiDocument document = ValidationFixture.document();
        Server server = new Server();
        server.setUrl("https://{environment}.example.com/{version}");
        ServerVariable environment = new ServerVariable();
        environment.setEnumValues(List.of("development", "staging"));
        environment.setDefaultValue("production?debug=true");
        server.getVariables().put("environment", environment);
        document.getServers().add(server);

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("server.variable.undefined"));
        assertTrue(codes.contains("server.variable.default-outside-enum"));
        assertTrue(codes.contains("server.url.query-or-fragment"));
    }

    private static Parameter parameter(
            String name,
            ParameterLocation location,
            boolean required) {
        Parameter parameter = new Parameter();
        parameter.setName(name);
        parameter.setLocation(location);
        parameter.setRequired(required);
        return parameter;
    }
}
