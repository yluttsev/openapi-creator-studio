package ru.luttsev.studio.application.validation;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.workspace.DocumentWorkspace;
import ru.luttsev.studio.core.validation.DocumentValidator;
import ru.luttsev.studio.core.validation.ValidationResult;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DocumentValidationService {

    private final DocumentWorkspace workspace;
    private final DocumentValidator validator;

    public DocumentValidation validate(UUID documentId) {
        DocumentValidation validation = workspace.inspect(documentId, session -> {
            ValidationResult result = validator.validate(session.document());
            return new DocumentValidation(
                    session.revision(),
                    result.isValid(),
                    result.issues());
        }).orElseThrow(() -> new DocumentNotFoundException(documentId));
        log.info(
                "Validated document {} at revision {}: valid={}, {} issue(s)",
                documentId,
                validation.revision(),
                validation.valid(),
                validation.issues().size());
        return validation;
    }
}
