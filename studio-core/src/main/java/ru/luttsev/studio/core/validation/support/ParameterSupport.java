package ru.luttsev.studio.core.validation.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.luttsev.studio.core.model.parameter.Parameter;
import ru.luttsev.studio.core.model.path.Operation;
import ru.luttsev.studio.core.model.path.PathItem;
import ru.luttsev.studio.core.model.reference.InlineObject;
import ru.luttsev.studio.core.model.reference.ReferenceObject;
import ru.luttsev.studio.core.model.reference.ReferenceOr;
import ru.luttsev.studio.core.navigation.DocumentPath;
import ru.luttsev.studio.core.validation.ValidationContext;

public final class ParameterSupport {

    private ParameterSupport() {
    }

    public static List<ParameterOccurrence> resolve(
            ValidationContext context,
            List<ReferenceOr<Parameter>> parameters,
            DocumentPath parametersPath) {
        ReferenceValueResolver resolver = new ReferenceValueResolver(context);
        ArrayList<ParameterOccurrence> resolvedParameters = new ArrayList<>();
        for (int index = 0; index < parameters.size(); index++) {
            ReferenceOr<Parameter> referenceOr = parameters.get(index);
            DocumentPath usagePath = parametersPath.child(Integer.toString(index));
            if (referenceOr instanceof InlineObject<Parameter> inlineObject) {
                resolvedParameters.add(new ParameterOccurrence(
                        inlineObject.value(),
                        usagePath,
                        usagePath));
                continue;
            }

            ReferenceObject<Parameter> referenceObject =
                    (ReferenceObject<Parameter>) referenceOr;
            if (referenceObject.getRef() == null) {
                continue;
            }
            resolver.resolveWithPath(referenceObject.getRef(), Parameter.class)
                    .ifPresent(resolved -> resolvedParameters.add(
                            new ParameterOccurrence(
                                    resolved.value(),
                                    usagePath,
                                    resolved.path())));
        }
        return List.copyOf(resolvedParameters);
    }

    public static List<ParameterOccurrence> effective(
            ValidationContext context,
            PathItem pathItem,
            DocumentPath pathItemPath,
            Operation operation,
            DocumentPath operationPath) {
        Map<ParameterKey, ParameterOccurrence> effective = new LinkedHashMap<>();
        for (ParameterOccurrence occurrence : resolve(
                context,
                pathItem.getParameters(),
                pathItemPath.child("parameters"))) {
            effective.put(occurrence.key(), occurrence);
        }
        for (ParameterOccurrence occurrence : resolve(
                context,
                operation.getParameters(),
                operationPath.child("parameters"))) {
            effective.put(occurrence.key(), occurrence);
        }
        return List.copyOf(effective.values());
    }
}
