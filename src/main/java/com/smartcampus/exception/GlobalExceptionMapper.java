package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.severe("Unexpected error: " + ex.getMessage());
        return Response.status(500).type(MediaType.APPLICATION_JSON)
                .entity(Map.of("status", 500, "error", "Internal Server Error",
                        "message", "An unexpected error occurred. Please contact the administrator."))
                .build();
    }
}