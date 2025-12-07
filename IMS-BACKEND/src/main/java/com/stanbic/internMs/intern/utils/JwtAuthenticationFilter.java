package com.stanbic.internMs.intern.utils;

import com.stanbic.internMs.intern.models.Role;
import com.stanbic.internMs.intern.repository.UserRepository;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserRepository userRepository) {
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

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUserIDFromToken(token);

                userRepository.findByEmail(username).ifPresent(user -> {

                    Set<GrantedAuthority> authorities = getAuthoritiesFromRole(user.getRole());

                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPasswordHash()) // not used here
                            .authorities(authorities)
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
    }

    /**
     * Map Role enum to Spring Security authorities
     */
    private Set<GrantedAuthority> getAuthoritiesFromRole(Role role) {
        if (role == null) return Collections.emptySet();

        return switch (role) {
            case ADMIN -> Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case INTERN -> Set.of(new SimpleGrantedAuthority("ROLE_INTERN"));
            case SUPERVISOR -> Set.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));
            case LINE_MANAGER -> Set.of(new SimpleGrantedAuthority("ROLE_LINE_MANAGER"));
        };
    }
}
