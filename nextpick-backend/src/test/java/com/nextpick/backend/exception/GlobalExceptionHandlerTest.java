package com.nextpick.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAuthenticationInputConflictAndRateLimitConsistently() {
        assertResponse(handler.handleBadCredentials(new BadCredentialsException("bad")), HttpStatus.UNAUTHORIZED);
        assertResponse(handler.handleUnreadableBody(new HttpMessageNotReadableException(
                        "bad json", new MockHttpInputMessage(new byte[0]))),
                HttpStatus.BAD_REQUEST);
        assertResponse(handler.handleDataConflict(new DataIntegrityViolationException("duplicate")),
                HttpStatus.CONFLICT);
        assertResponse(handler.handleUpstream(new UpstreamServiceException("TMDB", 429, "limit")),
                HttpStatus.TOO_MANY_REQUESTS);
    }

    private void assertResponse(org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response,
                                HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        assertThat(response.getBody()).containsKeys("timestamp", "status", "error");
        assertThat(response.getBody().get("status")).isEqualTo(expected.value());
    }
}
