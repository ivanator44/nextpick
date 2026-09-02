package com.nextpick.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class RestSecurityHandlersTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestSecurityHandlers handlers = new RestSecurityHandlers(mapper);

    @Test
    void writesJson401And403Contracts() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse unauthorized = new MockHttpServletResponse();
        handlers.commence(request, unauthorized, new BadCredentialsException("bad"));
        assertContract(unauthorized, 401);

        MockHttpServletResponse forbidden = new MockHttpServletResponse();
        handlers.handle(request, forbidden, new AccessDeniedException("forbidden"));
        assertContract(forbidden, 403);
    }

    private void assertContract(MockHttpServletResponse response, int status) throws Exception {
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getContentType()).startsWith("application/problem+json");
        JsonNode body = mapper.readTree(response.getContentAsString());
        assertThat(body.path("status").asInt()).isEqualTo(status);
        assertThat(body.path("error").asText()).isNotBlank();
        assertThat(body.path("timestamp").asText()).isNotBlank();
    }
}
