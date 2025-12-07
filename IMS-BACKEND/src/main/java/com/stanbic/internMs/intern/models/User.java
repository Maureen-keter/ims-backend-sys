package com.stanbic.internMs.intern.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fullName;
    private String userID;
    @NotBlank(message="Email cannot be empty")
    @Email()
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private Role role;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="department_id")
    private Department department;

    @OneToMany(mappedBy = "intern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceReview> internReviews;

    @OneToMany(mappedBy="lineManager", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceReview> managerReviews;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="cohort_id")
    private Cohort cohort;

        private Boolean archived = false;

        @ManyToMany
        @JoinTable(
            name="user_rotations",
            joinColumns =@JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="rotation_id")
        )
        private List<Rotation> rotations;


}
