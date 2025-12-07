package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.models.Role;
import com.stanbic.internMs.intern.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // List all users
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    // Paginated list
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // Paginated list with search
    public Page<User> listUsers(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByFullNameContainingIgnoreCase(search, pageable);
    }

    // Find by ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Find by userID
    public Optional<User> findByUserID(String userID) {
        return userRepository.findByUserID(userID);
    }

    // Create new user
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Save / update user
    public User save(User user) {
        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setFullName(user.getFullName());
                    existing.setEmail(user.getEmail());
                    existing.setPhoneNumber(user.getPhoneNumber());
                    if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
                        existing.setPasswordHash(user.getPasswordHash());
                    }
                    if (user.getRole() != null) {
                        existing.setRole(user.getRole());
                    }
                    return userRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Delete user
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
