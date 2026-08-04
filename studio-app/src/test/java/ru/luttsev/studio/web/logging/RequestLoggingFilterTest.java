package ru.luttsev.studio.web.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void generatesRequestIdWhenHeaderIsMissing() throws Exception {
        HttpServletRequest request = requestWithIncomingId(null);
        HttpServletResponse response = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        ArgumentCaptor<String> requestId = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(
                eq(RequestLoggingFilter.REQUEST_ID_HEADER),
                requestId.capture());
        assertThat(UUID.fromString(requestId.getValue())).isNotNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void reusesIncomingRequestId() throws Exception {
        HttpServletRequest request = requestWithIncomingId("existing-request-id");
        HttpServletResponse response = response();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader(
                RequestLoggingFilter.REQUEST_ID_HEADER,
                "existing-request-id");
    }

    @Test
    void populatesMdcDuringChainAndClearsItAfterwards() throws Exception {
        HttpServletRequest request = requestWithIncomingId("existing-request-id");
        HttpServletResponse response = response();
        FilterChain chain = mock(FilterChain.class);
        String[] mdcDuringChain = new String[1];
        doAnswer(invocation -> {
            mdcDuringChain[0] = MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY);
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        assertThat(mdcDuringChain[0]).isEqualTo("existing-request-id");
        assertThat(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    private HttpServletRequest requestWithIncomingId(String incomingRequestId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER))
                .thenReturn(incomingRequestId);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/documents");
        return request;
    }

    private HttpServletResponse response() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);
        return response;
    }
}
