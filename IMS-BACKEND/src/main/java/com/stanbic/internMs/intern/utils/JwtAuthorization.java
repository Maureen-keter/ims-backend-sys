package com.stanbic.internMs.intern.utils;

import com.stanbic.internMs.intern.repository.UserRepository;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthorization extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthorization(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth"); // skip /auth/**
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (DispatcherType.ERROR.equals(request.getDispatcherType())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Token: " + token);
            log.debug("Is token valid? " + jwtUtil.validateToken(token));

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUserIDFromToken(token);

                userRepository.findByEmail(username).ifPresent(user -> {
                    String roleName = "INTERN";
                    try {
                        if (user.getRole() != null) {
                            roleName = user.getRole().name();
                        }
                    } catch (Exception exception) {
                        log.error(exception.getMessage());
                    }
                    // Build UserDetails with roles
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                            .authorities(("ROLE_" + roleName).toUpperCase())
                            .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        filterChain.doFilter(request, response);

        // Debug
        log.debug("Authorization Header: " + request.getHeader("Authorization"));
        log.debug("SecurityContext auth after: " + SecurityContextHolder.getContext().getAuthentication());
    }

    //    @Override
    protected void doFilterInternalOld(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain)
            throws ServletException, IOException {

        if (DispatcherType.ERROR.equals(request.getDispatcherType())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Token: " + token);
            log.debug("Is token valid? " + jwtUtil.validateToken(token));

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUserIDFromToken(token);

                userRepository.findByUserID(username).ifPresent(user -> {
                    String roleName = "INTERN";
                    try {
                    if (user.getRole() != null) {
                        roleName = user.getRole().name();
                    }
                    } catch (Exception ignored) {}

                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(user.getUserID() == null ? user.getEmail() : user.getUserID())
                        .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                        .authorities(("ROLE_" + roleName).toUpperCase())
                        .build();

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        filterChain.doFilter(request, response);

        // Debug
        log.debug("Authorization Header: " + request.getHeader("Authorization"));
        log.debug("SecurityContext auth after: " + SecurityContextHolder.getContext().getAuthentication());
    }
}

