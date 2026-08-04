package ru.luttsev.studio.web.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.luttsev.studio.generated.model.ApiProblem;
import ru.luttsev.studio.generated.model.ExportRequest;
import ru.luttsev.studio.web.command.CommandResponseMapperImpl;
import ru.luttsev.studio.web.document.DiagnosticMapperImpl;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler(
                new DiagnosticMapperImpl(),
                new CommandResponseMapperImpl());
    }

    @Test
    void mapsMalformedBodyToSafeBadRequest() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Internal Jackson details",
                        mock(HttpInputMessage.class));

        ResponseEntity<ApiProblem> response =
                handler.handleUnreadableMessage(exception);

        assertProblem(
                response,
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST_BODY");
        assertThat(response.getBody().getDetail())
                .doesNotContain("Jackson");
    }

    @Test
    void mapsMissingIfMatchToPreconditionRequired() {
        MissingRequestHeaderException exception =
                new MissingRequestHeaderException(
                        "If-Match",
                        mock(MethodParameter.class));

        ResponseEntity<ApiProblem> response =
                handler.handleMissingRequestHeader(exception);

        assertProblem(
                response,
                HttpStatus.PRECONDITION_REQUIRED,
                "REVISION_REQUIRED");
    }

    @Test
    void includesFieldViolationsForInvalidRequestBody() {
        ExportRequest target = new ExportRequest();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "exportRequest");
        bindingResult.rejectValue(
                "format",
                "NotNull",
                "must not be null");
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        mock(MethodParameter.class),
                        bindingResult);

        ResponseEntity<ApiProblem> response =
                handler.handleInvalidMethodArgument(exception);

        assertProblem(
                response,
                HttpStatus.BAD_REQUEST,
                "REQUEST_VALIDATION_FAILED");
        assertThat(response.getBody().getViolations())
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getField()).isEqualTo("format");
                    assertThat(violation.getMessage())
                            .isEqualTo("must not be null");
                });
    }

    @Test
    void mapsIllegalArgumentToUniformBadRequest() {
        ResponseEntity<ApiProblem> response = handler.handleBadRequest(
                new IllegalArgumentException("Unsupported value"));

        assertProblem(response, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        assertThat(response.getBody().getDetail())
                .isEqualTo("Unsupported value");
    }

    private static void assertProblem(
            ResponseEntity<ApiProblem> response,
            HttpStatus status,
            String code) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(status.value());
        assertThat(response.getBody().getCode()).isEqualTo(code);
    }
}
