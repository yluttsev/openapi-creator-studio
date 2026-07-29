package ru.luttsev.studio.core.validation.profile;

import java.util.List;
import java.util.Objects;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.core.validation.ValidationRule;
import ru.luttsev.studio.core.validation.ValidationRulesProvider;
import ru.luttsev.studio.core.validation.rule.AmbiguousPathRule;
import ru.luttsev.studio.core.validation.rule.CallbackExpressionRule;
import ru.luttsev.studio.core.validation.rule.DiscriminatorRule;
import ru.luttsev.studio.core.validation.rule.EncodingContextRule;
import ru.luttsev.studio.core.validation.rule.LinkIntegrityRule;
import ru.luttsev.studio.core.validation.rule.OperationIdUniquenessRule;
import ru.luttsev.studio.core.validation.rule.ParameterUniquenessRule;
import ru.luttsev.studio.core.validation.rule.PathTemplateRule;
import ru.luttsev.studio.core.validation.rule.ReferenceIntegrityRule;
import ru.luttsev.studio.core.validation.rule.SecurityRequirementRule;
import ru.luttsev.studio.core.validation.rule.ServerRule;
import ru.luttsev.studio.core.validation.rule.TagUniquenessRule;

public final class OpenApi31ValidationRulesProvider
        implements ValidationRulesProvider {

    private static final List<ValidationRule> RULES = List.of(
            new ReferenceIntegrityRule(),
            new OperationIdUniquenessRule(),
            new PathTemplateRule(),
            new ParameterUniquenessRule(),
            new AmbiguousPathRule(),
            new TagUniquenessRule(),
            new SecurityRequirementRule(),
            new LinkIntegrityRule(),
            new CallbackExpressionRule(),
            new EncodingContextRule(),
            new DiscriminatorRule(),
            new ServerRule());

    @Override
    public List<ValidationRule> rulesFor(OpenApiVersion version) {
        Objects.requireNonNull(version, "version must not be null");
        return version.value().startsWith("3.1.")
                ? RULES
                : List.of();
    }
}
