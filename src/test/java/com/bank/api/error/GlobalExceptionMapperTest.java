package com.bank.api.error;

import com.bank.exception.AgeRuleViolationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionMapperTest {

    GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    void mapsAgeRuleViolationToBadRequest() {
        Response resp = mapper.toResponse(new AgeRuleViolationException("Age too low"));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());

        ErrorResponse err = (ErrorResponse) resp.getEntity();
        assertEquals("AGE_RULE_VIOLATION", err.errorCode);
        assertTrue(err.message.contains("Age too low"));
        assertNotNull(err.timestamp);
    }

    @Test
    void mapsUnknownToInternalError() {
        RuntimeException ex = new RuntimeException("boom");
        Response resp = mapper.toResponse(ex);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());

        ErrorResponse err = (ErrorResponse) resp.getEntity();
        assertEquals("INTERNAL_ERROR", err.errorCode);
        assertTrue(err.message.contains("boom"));
        assertNotNull(err.timestamp);
    }
}
