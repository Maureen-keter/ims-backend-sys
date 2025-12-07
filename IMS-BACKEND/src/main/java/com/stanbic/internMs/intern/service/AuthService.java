package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.repository.UserRepository;
import com.stanbic.internMs.intern.utils.JwtUtil;
import com.stanbic.internMs.intern.dto.DtoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Business logic for user registration
     */
    public User register(GenericDTO dto) {

        // Map dto to user
        User user = DtoMapper.mapToEntity(dto, User.class);

        // Unique checks
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException(Map.of("id", "User already exists with email " + user.getEmail()));
        }

        if (userRepository.findByUserID(user.getUserID()).isPresent()) {
            throw new ValidationException(Map.of("id", "UserID already exists: " + user.getUserID()));
        }

        // Fetch raw password from DTO
        String rawPassword = dto.getString("password");
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ValidationException(Map.of("id", "Password is required"));
        }

        // Hash and set
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }


    /**
     * Business logic for user login
     */
    public Map<String, Object> login(String emailOrUserID, String password) {
        User user = userRepository.findByEmail(emailOrUserID)
                .or(() -> userRepository.findByUserID(emailOrUserID))
                .orElseThrow(() -> new ValidationException(Map.of("id", "Invalid credentials")));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ValidationException(Map.of("id", "Invalid credentials"));
        }

        // Generate JWT token
        String jwt = jwtUtil.generateToken(user.getUserID());

        return Map.of(
                "access_token", jwt,
                "token_type", "Bearer",
                "status", 200,
                "loginStatus", "successful"
//                "name", user.getName()
        );
    }
}
