package com.example.exception;

import io.quarkus.logging.Log;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception mapper that converts application exceptions into
 * structured JSON error responses.
 */
@Provider
public class GlobalExceptionMapper {

    /**
     * Handles {@link ResourceNotFoundException} → 404 Not Found.
     */
    @Provider
    public static class NotFoundMapper
        implements ExceptionMapper<ResourceNotFoundException>
    {
        @Override
        public Response toResponse(ResourceNotFoundException ex) {
            return errorResponse(
                Response.Status.NOT_FOUND,
                ex.getMessage(),
                null
            );
        }
    }

    /**
     * Handles standard JAX-RS {@link NotFoundException} → 404 Not Found.
     */
    @Provider
    public static class JaxRsNotFoundMapper
        implements ExceptionMapper<NotFoundException>
    {
        @Override
        public Response toResponse(NotFoundException ex) {
            return errorResponse(
                Response.Status.NOT_FOUND,
                ex.getMessage(),
                null
            );
        }
    }

    /**
     * Handles {@link ConflictException} → 409 Conflict.
     */
    @Provider
    public static class ConflictMapper
        implements ExceptionMapper<ConflictException>
    {
        @Override
        public Response toResponse(ConflictException ex) {
            return errorResponse(
                Response.Status.CONFLICT,
                ex.getMessage(),
                null
            );
        }
    }

    /**
     * Handles Jakarta Bean Validation errors → 400 Bad Request with field details.
     */
    @Provider
    public static class ValidationMapper
        implements ExceptionMapper<ConstraintViolationException>
    {
        @Override
        public Response toResponse(ConstraintViolationException ex) {
            Map<String, String> violations = ex
                .getConstraintViolations()
                .stream()
                .collect(
                    Collectors.toMap(
                        v -> extractFieldName(v.getPropertyPath().toString()),
                        v -> v.getMessage(),
                        (a, b) -> a
                    )
                );

            return errorResponse(
                Response.Status.BAD_REQUEST,
                "Validation failed",
                violations
            );
        }

        private String extractFieldName(String propertyPath) {
            String[] parts = propertyPath.split("\\.");
            return parts[parts.length - 1];
        }
    }

    /**
     * Catch-all for unhandled exceptions → 500 Internal Server Error.
     */
    @Provider
    public static class GeneralMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception ex) {
            if (ex instanceof WebApplicationException webEx) {
                Response response = webEx.getResponse();
                Response.Status status = Response.Status.fromStatusCode(
                    response.getStatus()
                );

                return errorResponse(status, webEx.getMessage(), null);
            }

            Log.error("Unhandled exception: " + ex.toString(), ex);

            return errorResponse(
                Response.Status.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                null
            );
        }
    }

    // ---- helpers ----

    static Response errorResponse(
        Response.Status status,
        String message,
        Object details
    ) {
        var body = new ErrorBody(
            status.getStatusCode(),
            status.getReasonPhrase(),
            message,
            details,
            LocalDateTime.now().toString()
        );

        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(body)
            .build();
    }

    /** Immutable error response body (Java 21 record). */
    public record ErrorBody(
        int status,
        String error,
        String message,
        Object details,
        String timestamp
    ) {}
}
