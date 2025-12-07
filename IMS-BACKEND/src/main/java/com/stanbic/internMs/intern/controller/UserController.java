package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.DtoMapper;
import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.Role;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.service.UserService;
import com.stanbic.internMs.intern.utils.ValidationUtil;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<StandardAPIResponse> all() {
        try {
            return ResponseEntity.ok(
                    StandardAPIResponse.builder()
                            .data(userService.listUsers())
                            .message("Successfully fetched users")
                            .successful(true)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/view/{id}")
    public ResponseEntity<StandardAPIResponse> getUser(@PathVariable("id") @Min(1) Long id) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isEmpty()) {
                throw new ValidationException(Map.of("id", "User not found with id " + id));
            }
            return ResponseEntity.ok(
                    StandardAPIResponse.builder()
                            .data(user.get())
                            .message("Successfully retrieved user")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve) {
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("id"))
                            .successful(false)
                            .message("User record not found")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<StandardAPIResponse> update(@PathVariable Long id, @RequestBody GenericDTO dto){
        try {
            Optional<User> optionalUser = userService.findById(id);
            if (optionalUser.isEmpty()) {
                throw new ValidationException(Map.of("id", "User not found with id " + id));
            }

            User user = optionalUser.get();

            // Map fields from DTO
            ValidationUtil.validateProvidedFields(dto, User.class);
            DtoMapper.mapNonNullFields(dto, user);

            // Handle password separately
            String rawPassword = dto.getString("password");
            if(rawPassword != null && !rawPassword.isBlank()){
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
            }

            // Handle role separately
            String roleStr = dto.getString("role");
            if(roleStr != null && !roleStr.isBlank()){
                try {
                    user.setRole(Role.valueOf(roleStr.toUpperCase()));
                } catch(Exception ignored) {}
            }

            User updated = userService.save(user);
            return ResponseEntity.ok(
                    StandardAPIResponse.builder()
                            .data(updated)
                            .message("User updated successfully")
                            .successful(true)
                            .build()
            );

        } catch (ValidationException ve){
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("id"))
                            .successful(false)
                            .message("User record not found")
                            .build()
            );
        } catch(Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<StandardAPIResponse> createUser(@RequestBody GenericDTO dto){
        try{
            ValidationUtil.validateRequiredFields(dto, User.class);
            User user = DtoMapper.mapToEntity(dto, User.class);

            // Handle password
            String rawPassword = dto.getString("password");
            if(rawPassword != null && !rawPassword.isBlank()){
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
            }

            // Handle role
            String roleStr = dto.getString("role");
            if(roleStr != null && !roleStr.isBlank()){
                try {
                    user.setRole(Role.valueOf(roleStr.toUpperCase()));
                } catch(Exception ignored) {
                    user.setRole(Role.INTERN);
                }
            } else {
                user.setRole(Role.INTERN);
            }

            User created = userService.createUser(user);
            return ResponseEntity.ok(
                    StandardAPIResponse.builder()
                            .data(created)
                            .message("User created successfully")
                            .successful(true)
                            .build()
            );
        } catch(Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<StandardAPIResponse> registerUser(@RequestBody GenericDTO dto){
        try{
            ValidationUtil.validateRequiredFields(dto, User.class);
            User user = DtoMapper.mapToEntity(dto, User.class);

            // require password
            String rawPassword = dto.getString("password");
            if(rawPassword == null || rawPassword.isBlank()){
                return ResponseEntity.badRequest().body(
                        StandardAPIResponse.builder()
                                .successful(false)
                                .message("Password is required")
                                .build()
                );
            }

            user.setPasswordHash(passwordEncoder.encode(rawPassword));

            // map role if provided, default INTERN
            String roleStr = dto.getString("role");
            if(roleStr != null && !roleStr.isBlank()){
                try {
                    user.setRole(Role.valueOf(roleStr.toUpperCase()));
                } catch(Exception e) {
                    user.setRole(Role.INTERN);
                }
            } else {
                user.setRole(Role.INTERN);
            }

            User created = userService.createUser(user);
            return ResponseEntity.status(201).body(
                    StandardAPIResponse.builder()
                            .data(created)
                            .successful(true)
                            .message("User registered successfully")
                            .build()
            );
        } catch(Exception e){
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
