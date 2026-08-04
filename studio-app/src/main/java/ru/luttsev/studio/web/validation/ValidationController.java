package ru.luttsev.studio.web.validation;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.luttsev.studio.application.validation.DocumentValidation;
import ru.luttsev.studio.application.validation.DocumentValidationService;
import ru.luttsev.studio.generated.api.ValidationApi;
import ru.luttsev.studio.generated.model.ValidationResponse;
import ru.luttsev.studio.web.document.RevisionHeader;

@RestController
@RequiredArgsConstructor
public class ValidationController implements ValidationApi {

    private final DocumentValidationService validationService;
    private final ValidationResponseMapper responseMapper;

    @Override
    public ResponseEntity<ValidationResponse> validateDocument(UUID documentId) {
        DocumentValidation validation = validationService.validate(documentId);
        return ResponseEntity.ok()
                .eTag(RevisionHeader.format(validation.revision()))
                .body(responseMapper.map(validation));
    }
}
