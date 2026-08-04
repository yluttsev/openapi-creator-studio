package ru.luttsev.studio.web.document;

import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.luttsev.studio.application.document.DocumentLifecycleService;
import ru.luttsev.studio.application.document.DocumentRepresentationService;
import ru.luttsev.studio.application.document.OpenedDocument;
import ru.luttsev.studio.application.workspace.DocumentSession;
import ru.luttsev.studio.core.model.OpenApiVersion;
import ru.luttsev.studio.generated.api.DocumentsApi;
import ru.luttsev.studio.generated.model.CreateDocumentRequest;
import ru.luttsev.studio.generated.model.DocumentResponse;
import ru.luttsev.studio.generated.model.OpenDocumentResponse;

@RestController
@RequiredArgsConstructor
public class DocumentsController implements DocumentsApi {

    private final DocumentLifecycleService lifecycleService;
    private final DocumentRepresentationService representationService;
    private final DocumentResponseMapper responseMapper;

    @Override
    public ResponseEntity<OpenDocumentResponse> createDocument(
            CreateDocumentRequest request) {
        String versionValue = request.getOpenApiVersion() == null
                ? OpenApiVersion.V3_1_2.value()
                : request.getOpenApiVersion();
        OpenedDocument openedDocument = lifecycleService.create(
                new OpenApiVersion(versionValue),
                request.getTitle(),
                request.getApiVersion(),
                request.getDescription());
        return created(openedDocument);
    }

    @Override
    public ResponseEntity<OpenDocumentResponse> importDocument(String body) {
        return created(lifecycleService.importDocument(body));
    }

    @Override
    public ResponseEntity<DocumentResponse> getDocument(UUID documentId) {
        DocumentSession session = lifecycleService.get(documentId);
        DocumentResponse response = responseMapper.toDocumentResponse(
                session,
                representationService.represent(session.document()));
        return ResponseEntity.ok()
                .eTag(RevisionHeader.format(session.revision()))
                .body(response);
    }

    @Override
    public ResponseEntity<Void> closeDocument(
            String ifMatch,
            UUID documentId) {
        lifecycleService.close(documentId, RevisionHeader.parse(ifMatch));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<OpenDocumentResponse> created(
            OpenedDocument openedDocument) {
        DocumentSession session = openedDocument.session();
        OpenDocumentResponse response =
                responseMapper.toOpenDocumentResponse(openedDocument);
        URI location = UriComponentsBuilder
                .fromPath("")
                .path("/api/v1/documents/{documentId}")
                .buildAndExpand(session.id())
                .toUri();
        return ResponseEntity.created(location)
                .header(
                        HttpHeaders.ETAG,
                        RevisionHeader.format(session.revision()))
                .body(response);
    }
}
