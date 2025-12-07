package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
    Optional<User> findByUserID(String userID);
    Optional<User> findByEmail(String email);
}

