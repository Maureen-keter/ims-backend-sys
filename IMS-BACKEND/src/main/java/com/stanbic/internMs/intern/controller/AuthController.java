package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.DtoMapper;
import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.dto.LoginRequest;
import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.Role;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.service.AuthService;
import com.stanbic.internMs.intern.service.UserService;
import com.stanbic.internMs.intern.utils.ValidationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/v1")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService=userService;
    }

    /**
     * Signup endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<StandardAPIResponse> login(@RequestBody GenericDTO dto) {
        try {
            String email = dto.getString("email");
            String password = dto.getString("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(
                        StandardAPIResponse.builder()
                                .successful(false)
                                .message("Email and password are required")
                                .build()
                );
            }

            Map<String, Object> loginData = authService.login(email, password);

            return ResponseEntity.ok(
                    StandardAPIResponse.builder()
                            .successful(true)
                            .message("Login successful")
                            .data(loginData)
                            .build()
            );

        } catch (ValidationException ve) {
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .successful(false)
                            .message(ve.getErrors().get("id"))
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .successful(false)
                            .message("Something went wrong during login")
                            .errors(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<StandardAPIResponse> registerUser(@RequestBody GenericDTO dto){
        try {
            User created = authService.register(dto);

            return ResponseEntity.status(201).body(
                    StandardAPIResponse.builder()
                            .data(created)
                            .successful(true)
                            .message("User registered successfully")
                            .build()
            );

        } catch (ValidationException ve) {
            return ResponseEntity.badRequest().body(
                    StandardAPIResponse.builder()
                            .successful(false)
                            .message(ve.getErrors().get("id"))
                            .build()
            );

        } catch(Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

}