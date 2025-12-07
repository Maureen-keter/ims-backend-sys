package com.stanbic.internMs.intern.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name="rotations")
@AllArgsConstructor
@NoArgsConstructor
public class Rotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="cohort_id")
    private Cohort cohort;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="department_id", nullable = false)
    private Department department;

    @ManyToMany(mappedBy = "rotations")
    private List<User> interns;

    @OneToMany(mappedBy = "rotation", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceReview> performanceReviews;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_supervisor_id")
    private User assignedSupervisor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id")
    private User approver;

    private java.time.Instant createdAt = java.time.Instant.now();
    private java.time.Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PROPOSED;

    public enum Status{
        PROPOSED, APPROVED, REJECTED, IN_PROGRESS, COMPLETED
    }

}
