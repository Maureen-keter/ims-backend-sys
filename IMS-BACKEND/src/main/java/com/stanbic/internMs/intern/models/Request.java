package com.stanbic.internMs.intern.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "intern_id", nullable = false)
    private User intern;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responder_id")
    private User responder;

    @Column(length = 1000)
    private String responseReason;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
