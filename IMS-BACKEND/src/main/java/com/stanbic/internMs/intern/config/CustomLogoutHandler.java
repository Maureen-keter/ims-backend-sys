package com.stanbic.internMs.intern.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutHandler extends SecurityContextLogoutHandler {

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        // Clears SecurityContext
        super.logout(request, response, authentication);

        // OPTIONAL: Clear JWT cookie if you use cookies
        // Cookie cookie = new Cookie("jwt", null);
        // cookie.setMaxAge(0);
        // cookie.setPath("/");
        // response.addCookie(cookie);

        // Redirect user after logout
        try {
            response.sendRedirect("/login"); // Change to your frontend URL if needed
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
