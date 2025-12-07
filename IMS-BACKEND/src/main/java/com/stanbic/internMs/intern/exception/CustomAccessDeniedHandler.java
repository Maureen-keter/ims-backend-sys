package com.stanbic.internMs.intern.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stanbic.internMs.intern.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ApiError error = new ApiError(
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                accessDeniedException.getMessage(),
                request.getRequestURI()
        );

        // Wrap in ResponseEntity and delegate writing to Spring
        ResponseEntity<ApiError> entity =
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        // Manually write it out (since AuthenticationEntryPoint does not return ResponseEntity directly)
        response.setStatus(entity.getStatusCodeValue());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), entity.getBody());
    }
}

