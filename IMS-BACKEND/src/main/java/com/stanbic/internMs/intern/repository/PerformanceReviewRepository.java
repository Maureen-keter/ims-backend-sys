package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.PerformanceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview,Long> {
    // Search by intern full name (User.fullName)
    Page<PerformanceReview> findByIntern_FullNameContainingIgnoreCase(String search, Pageable pageable);

    // Find all reviews for a specific intern
    List<PerformanceReview> findByInternId(Long internId);

    Page<PerformanceReview> findByInternId(Long internId, Pageable pageable);
    
    // Find reviews by who created/owned them (line manager)
    List<PerformanceReview> findByLineManagerId(Long managerId);

    Page<PerformanceReview> findByLineManagerId(Long managerId, Pageable pageable);
    
    // Efficient existence check for a review authored by manager for a specific intern
    boolean existsByLineManagerIdAndIntern_Id(Long managerId, Long internId);
}
