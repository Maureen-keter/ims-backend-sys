package com.stanbic.internMs.intern.exception;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.stanbic.internMs.intern.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 👇 If it's really a missing endpoint, don’t force 401
        if (request.getAttribute("jakarta.servlet.error.status_code") != null) {
            log.info("status_code::"+request.getAttribute("jakarta.servlet.error.status_code"));
            return;
        }

        log.info("AuthenticationException::" + authException.getMessage() + " isAuthenticated::" + authException.getAuthenticationRequest().isAuthenticated());
        log.info("error::" + response.getStatus());

        ApiError error = new ApiError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                authException.getMessage(),
                request.getRequestURI()
        );

        if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
            error.setError("Not Found");
            error.setMessage("URI Doesn't exist");
            error.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.info("error 404::"+error.getStatus());
            return;
        } else if (response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {

        }

        log.info("error::"+error.getStatus());

        // Wrap in ResponseEntity and delegate writing to Spring
        ResponseEntity<ApiError> entity =
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        // Manually write it out (since AuthenticationEntryPoint does not return ResponseEntity directly)
        response.setStatus(entity.getStatusCodeValue());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), entity.getBody());
    }
}

