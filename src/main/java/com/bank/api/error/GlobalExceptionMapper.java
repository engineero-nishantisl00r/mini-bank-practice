package com.bank.api.error;

import com.bank.exception.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException ex) {

        if (ex instanceof AccountNotFoundException) {
            return build(Response.Status.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex);
        }

        if (ex instanceof InvalidProductException) {
            return build(Response.Status.BAD_REQUEST, "INVALID_PRODUCT", ex);
        }

        if (ex instanceof AgeRuleViolationException) {
            return build(Response.Status.BAD_REQUEST, "AGE_RULE_VIOLATION", ex);
        }

        if (ex instanceof AmountRuleViolationException) {
            return build(Response.Status.BAD_REQUEST, "AMOUNT_RULE_VIOLATION", ex);
        }

        if (ex instanceof InsufficientBalanceException) {
            return build(Response.Status.BAD_REQUEST, "INSUFFICIENT_BALANCE", ex);
        }

        // fallback (unexpected errors) — log stacktrace to aid debugging
        if (!(ex instanceof AccountNotFoundException || ex instanceof InvalidProductException
                || ex instanceof AgeRuleViolationException || ex instanceof AmountRuleViolationException
                || ex instanceof InsufficientBalanceException)) {
            LOG.error("Unhandled exception returning 500", ex);
        }

        return build(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex);
    }

    private Response build(Response.Status status, String errorCode, RuntimeException ex) {
        return Response.status(status)
                .entity(new ErrorResponse(errorCode, ex.getMessage()))
                .build();
    }
}
