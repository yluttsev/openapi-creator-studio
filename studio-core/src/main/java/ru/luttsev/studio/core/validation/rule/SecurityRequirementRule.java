package ru.luttsev.studio.core.validation.rule;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.luttsev.studio.core.model.Components;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.model.security.OAuthFlow;
import ru.luttsev.studio.core.model.security.OAuthFlows;
import ru.luttsev.studio.core.model.security.SecurityRequirement;
import ru.luttsev.studio.core.model.security.SecurityScheme;
import ru.luttsev.studio.core.model.security.SecuritySchemeType;
import ru.luttsev.studio.core.navigation.DocumentEntry;
import ru.luttsev.studio.core.validation.ValidationCode;
import ru.luttsev.studio.core.validation.ValidationContext;
import ru.luttsev.studio.core.validation.ValidationIssue;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationSeverity;
import ru.luttsev.studio.core.validation.support.DocumentEntries;
import ru.luttsev.studio.core.validation.support.ReferenceValueResolver;

public final class SecurityRequirementRule implements ValidationRule {

    private static final ValidationCode UNKNOWN_SCHEME =
            new ValidationCode("security.requirement.scheme.unknown");
    private static final ValidationCode UNKNOWN_SCOPE =
            new ValidationCode("security.requirement.oauth2-scope.unknown");

    @Override
    public List<ValidationIssue> validate(ValidationContext context) {
        ArrayList<ValidationIssue> issues = new ArrayList<>();
        Components components = context.document().getComponents();
        Map<String, ReferenceOr<SecurityScheme>> schemes = components == null
                ? Map.of()
                : components.getSecuritySchemes();
        ReferenceValueResolver resolver = new ReferenceValueResolver(context);

        for (DocumentEntry entry : DocumentEntries.ofType(
                context,
                SecurityRequirement.class)) {
            SecurityRequirement requirement = (SecurityRequirement) entry.value();
            for (Map.Entry<String, List<String>> requiredScheme :
                    requirement.getRequirements().entrySet()) {
                ReferenceOr<SecurityScheme> schemeReference =
                        schemes.get(requiredScheme.getKey());
                if (schemeReference == null) {
                    issues.add(new ValidationIssue(
                            UNKNOWN_SCHEME,
                            ValidationSeverity.ERROR,
                            "Security requirement references an unknown scheme: "
                                    + requiredScheme.getKey(),
                            entry.path().child(requiredScheme.getKey())));
                    continue;
                }

                resolver.resolve(schemeReference, SecurityScheme.class)
                        .filter(scheme -> scheme.getType() == SecuritySchemeType.OAUTH2)
                        .ifPresent(scheme -> validateScopes(
                                scheme,
                                requiredScheme,
                                entry,
                                issues));
            }
        }
        return List.copyOf(issues);
    }

    private static void validateScopes(
            SecurityScheme scheme,
            Map.Entry<String, List<String>> requiredScheme,
            DocumentEntry entry,
            List<ValidationIssue> issues) {
        Set<String> declaredScopes = collectScopes(scheme.getFlows());
        for (String scope : requiredScheme.getValue()) {
            if (!declaredScopes.contains(scope)) {
                issues.add(new ValidationIssue(
                        UNKNOWN_SCOPE,
                        ValidationSeverity.ERROR,
                        "OAuth2 scope is not declared by the security scheme: " + scope,
                        entry.path().child(requiredScheme.getKey())));
            }
        }
    }

    private static Set<String> collectScopes(OAuthFlows flows) {
        if (flows == null) {
            return Set.of();
        }
        LinkedHashSet<String> scopes = new LinkedHashSet<>();
        addScopes(scopes, flows.getImplicit());
        addScopes(scopes, flows.getPassword());
        addScopes(scopes, flows.getClientCredentials());
        addScopes(scopes, flows.getAuthorizationCode());
        addScopes(scopes, flows.getDeviceAuthorization());
        return scopes;
    }

    private static void addScopes(Set<String> scopes, OAuthFlow flow) {
        if (flow != null) {
            scopes.addAll(flow.getScopes().keySet());
        }
    }
}
