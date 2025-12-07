package com.stanbic.internMs.intern.config;

import com.stanbic.internMs.intern.exception.CustomAccessDeniedHandler;
import com.stanbic.internMs.intern.exception.CustomAuthHandler;
import com.stanbic.internMs.intern.utils.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthHandler customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomLogoutHandler logoutHandler;

    @Value("${ims.internMs.allowed-origins}")
    private String allowedOrigins;

    @Value("${client-login-page}")
    private String loginPage;

    public SecurityConfig(CustomAuthHandler customAuthEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomLogoutHandler logoutHandler) {
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/v1/register",
                                "/auth/v1/login",
                                "/error",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                                .anyRequest().permitAll()
//                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl(loginPage)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
