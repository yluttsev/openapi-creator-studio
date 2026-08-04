package ru.luttsev.studio.web.error;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.luttsev.studio.application.command.CommandRejectedException;
import ru.luttsev.studio.application.document.DocumentNotFoundException;
import ru.luttsev.studio.application.document.OpenApiProcessingException;
import ru.luttsev.studio.application.workspace.RevisionConflictException;
import ru.luttsev.studio.generated.model.ApiProblem;
import ru.luttsev.studio.generated.model.CommandRejectedProblem;
import ru.luttsev.studio.generated.model.OpenApiProcessingProblem;
import ru.luttsev.studio.generated.model.RequestViolation;
import ru.luttsev.studio.web.command.CommandResponseMapper;
import ru.luttsev.studio.web.document.DiagnosticMapper;

@RestControllerAdvice
@RequiredArgsConstructor
public final class ApiExceptionHandler {

    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private final DiagnosticMapper diagnosticMapper;
    private final CommandResponseMapper commandResponseMapper;

    @ExceptionHandler(CommandRejectedException.class)
    ResponseEntity<CommandRejectedProblem> handleCommandRejected(
            CommandRejectedException exception) {
        CommandRejectedProblem problem = new CommandRejectedProblem(
                ABOUT_BLANK,
                "Command rejected",
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                "COMMAND_REJECTED",
                commandResponseMapper.map(exception.getIssues()));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiProblem> handleUnreadableMessage(
            HttpMessageNotReadableException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                "The request body is missing or contains invalid JSON",
                "MALFORMED_REQUEST_BODY");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ApiProblem> handleMissingRequestHeader(
            MissingRequestHeaderException exception) {
        if ("If-Match".equalsIgnoreCase(exception.getHeaderName())) {
            return problem(
                    HttpStatus.PRECONDITION_REQUIRED,
                    "Revision required",
                    "The If-Match revision header is required",
                    "REVISION_REQUIRED");
        }
        return badRequest(
                "Required header is missing",
                List.of(new RequestViolation(
                        exception.getHeaderName(),
                        "Header is required")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiProblem> handleInvalidMethodArgument(
            MethodArgumentNotValidException exception) {
        ArrayList<RequestViolation> violations = new ArrayList<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            violations.add(new RequestViolation(
                    error.getField(),
                    message(error)));
        }
        exception.getBindingResult().getGlobalErrors().forEach(error ->
                violations.add(new RequestViolation(
                        error.getObjectName(),
                        message(error))));
        return badRequest("Request validation failed", violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiProblem> handleMethodValidation(
            HandlerMethodValidationException exception) {
        ArrayList<RequestViolation> violations = new ArrayList<>();
        exception.getParameterValidationResults().forEach(result -> {
            String parameter = result.getMethodParameter().getParameterName();
            String field = parameter == null ? "request" : parameter;
            result.getResolvableErrors().forEach(error ->
                    violations.add(new RequestViolation(
                            field,
                            message(error))));
        });
        return badRequest("Request validation failed", violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiProblem> handleConstraintViolation(
            ConstraintViolationException exception) {
        ArrayList<RequestViolation> violations = new ArrayList<>();
        for (ConstraintViolation<?> violation :
                exception.getConstraintViolations()) {
            violations.add(new RequestViolation(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()));
        }
        return badRequest("Request validation failed", violations);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiProblem> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        return badRequest(
                "Request parameter has an invalid value",
                List.of(new RequestViolation(
                        exception.getName(),
                        "Value has an invalid type or format")));
    }

    @ExceptionHandler(OpenApiProcessingException.class)
    ResponseEntity<OpenApiProcessingProblem> handleOpenApiProcessing(
            OpenApiProcessingException exception) {
        OpenApiProcessingProblem problem = new OpenApiProcessingProblem(
                ABOUT_BLANK,
                "OpenAPI processing failed",
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                exception.getMessage(),
                "OPENAPI_PROCESSING_FAILED",
                diagnosticMapper.map(exception.getDiagnostics()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
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

    private ResponseEntity<ApiProblem> badRequest(
            String detail,
            List<RequestViolation> violations) {
        ApiProblem problem = new ApiProblem(
                ABOUT_BLANK,
                "Bad request",
                HttpStatus.BAD_REQUEST.value(),
                detail,
                "REQUEST_VALIDATION_FAILED");
        problem.setViolations(violations);
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private String message(MessageSourceResolvable error) {
        String defaultMessage = error.getDefaultMessage();
        return defaultMessage == null ? "Invalid value" : defaultMessage;
    }
}
