package ru.luttsev.studio.web.error;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.document.OpenApiProcessingException;
import ru.luttsev.studio.application.workspace.RevisionConflictException;
import ru.luttsev.studio.generated.model.ApiProblem;
import ru.luttsev.studio.generated.model.OpenApiProcessingProblem;
import ru.luttsev.studio.web.document.DiagnosticMapper;

@RestControllerAdvice
@RequiredArgsConstructor
public final class ApiExceptionHandler {

    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private final DiagnosticMapper diagnosticMapper;

    @ExceptionHandler(DocumentNotFoundException.class)
    ResponseEntity<ApiProblem> handleNotFound(
            DocumentNotFoundException exception) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Document not found",
                exception.getMessage(),
                "DOCUMENT_NOT_FOUND");
    }

    @ExceptionHandler(RevisionConflictException.class)
    ResponseEntity<ApiProblem> handleRevisionConflict(
            RevisionConflictException exception) {
        return problem(
                HttpStatus.PRECONDITION_FAILED,
                "Revision mismatch",
                exception.getMessage(),
                "REVISION_MISMATCH");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiProblem> handleBadRequest(
            IllegalArgumentException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Bad request",
                exception.getMessage(),
                "BAD_REQUEST");
    }

    @ExceptionHandler(OpenApiProcessingException.class)
    ResponseEntity<OpenApiProcessingProblem> handleOpenApiProcessing(
            OpenApiProcessingException exception) {
        OpenApiProcessingProblem problem = new OpenApiProcessingProblem(
                ABOUT_BLANK,
                "OpenAPI processing failed",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                exception.getMessage(),
                "OPENAPI_PROCESSING_FAILED",
                diagnosticMapper.map(exception.getDiagnostics()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private ResponseEntity<ApiProblem> problem(
            HttpStatus status,
            String title,
            String detail,
            String code) {
        ApiProblem problem = new ApiProblem(
                ABOUT_BLANK,
                title,
                status.value(),
                detail,
                code);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
