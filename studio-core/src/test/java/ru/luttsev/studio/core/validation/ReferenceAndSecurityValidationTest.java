package ru.luttsev.studio.core.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import ru.luttsev.studio.core.model.OpenApiDocument;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.schema.SchemaDefinition;
import ru.luttsev.studio.core.model.schema.UriReference;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;

class ReferenceAndSecurityValidationTest {

    @Test
    void reportsMissingUncheckedAndWronglyTypedReferences() {
        OpenApiDocument document = ValidationFixture.document();
        document.getComponents().getSchemas().put("User", new SchemaDefinition());

        ReferenceObject<Parameter> wrongType = new ReferenceObject<>();
        wrongType.setRef(new UriReference("#/components/schemas/User"));
        document.getComponents().getParameters().put("Wrong", wrongType);

        ReferenceObject<Parameter> missing = new ReferenceObject<>();
        missing.setRef(new UriReference("#/components/parameters/Missing"));
        document.getComponents().getParameters().put("MissingReference", missing);

        ReferenceObject<Parameter> external = new ReferenceObject<>();
        external.setRef(new UriReference("shared.yaml#/components/parameters/User"));
        document.getComponents().getParameters().put("External", external);

        ValidationResult result = new DocumentValidator().validate(document);
        Set<String> codes = ValidationFixture.codes(result);

        assertTrue(codes.contains("reference.target.type-mismatch"));
        assertTrue(codes.contains("reference.target.not-found"));
        assertTrue(codes.contains("reference.target.unchecked"));
        assertFalse(result.warnings().isEmpty());
    }

    @Test
    void allowsLocalReferenceCycles() {
        OpenApiDocument document = ValidationFixture.document();
        SchemaDefinition first = new SchemaDefinition();
        first.setRef(new UriReference("#/components/schemas/Second"));
        SchemaDefinition second = new SchemaDefinition();
        second.setRef(new UriReference("#/components/schemas/First"));
        document.getComponents().getSchemas().put("First", first);
        document.getComponents().getSchemas().put("Second", second);

        ValidationResult result = new DocumentValidator().validate(document);

        assertTrue(result.isValid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void treatsAnExternalReferenceAsUncheckedWarning() {
        OpenApiDocument document = ValidationFixture.document();
        SchemaDefinition external = new SchemaDefinition();
        external.setRef(new UriReference(
                "shared.yaml#/components/schemas/User"));
        document.getComponents().getSchemas().put("User", external);

        ValidationResult result = new DocumentValidator().validate(document);

        assertTrue(result.isValid());
        assertTrue(ValidationFixture.codes(result).contains(
                "reference.target.unchecked"));
    }

    @Test
    void validatesSecuritySchemeNamesAndOAuth2Scopes() {
        OpenApiDocument document = ValidationFixture.document();
        SecurityScheme oauth2 = new SecurityScheme();
        oauth2.setType(SecuritySchemeType.OAUTH2);
        OAuthFlow authorizationCode = new OAuthFlow();
        authorizationCode.getScopes().put("users:read", "Read users");
        OAuthFlows flows = new OAuthFlows();
        flows.setAuthorizationCode(authorizationCode);
        oauth2.setFlows(flows);
        document.getComponents()
                .getSecuritySchemes()
                .put("oauth", ValidationFixture.inline(oauth2));

        SecurityRequirement requirement = new SecurityRequirement();
        requirement.require("oauth", List.of("users:write"));
        requirement.require("missing", List.of());
        document.getSecurity().add(requirement);

        Set<String> codes =
                ValidationFixture.codes(new DocumentValidator().validate(document));

        assertTrue(codes.contains("security.requirement.scheme.unknown"));
        assertTrue(codes.contains("security.requirement.oauth2-scope.unknown"));
    }
}
