package ru.luttsev.studio.web.exporting;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.luttsev.studio.application.exporting.DocumentExport;
import ru.luttsev.studio.application.exporting.DocumentExportService;
import ru.luttsev.studio.generated.api.ExportsApi;
import ru.luttsev.studio.generated.model.ExportRequest;
import ru.luttsev.studio.generated.model.ExportResponse;
import ru.luttsev.studio.web.document.RevisionHeader;

@RestController
@RequiredArgsConstructor
public class ExportController implements ExportsApi {

    private final DocumentExportService exportService;
    private final ExportMapper exportMapper;

    @Override
    public ResponseEntity<ExportResponse> exportDocument(
            UUID documentId,
            ExportRequest exportRequest) {
        DocumentExport documentExport = exportService.export(
                documentId,
                exportMapper.map(exportRequest.getFormat()),
                exportMapper.map(exportRequest.getTargetVersion()));
        return ResponseEntity.ok()
                .eTag(RevisionHeader.format(documentExport.revision()))
                .body(exportMapper.map(documentExport));
    }
}
