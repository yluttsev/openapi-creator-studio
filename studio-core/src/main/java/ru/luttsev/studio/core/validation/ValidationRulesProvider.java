package ru.luttsev.studio.core.validation;

import java.util.List;
import ru.luttsev.studio.core.model.OpenApiVersion;

public interface ValidationRulesProvider {

    List<ValidationRule> rulesFor(OpenApiVersion version);
}
