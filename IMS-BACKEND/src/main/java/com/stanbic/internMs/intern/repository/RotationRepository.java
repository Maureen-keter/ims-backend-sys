package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.Rotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RotationRepository extends JpaRepository<Rotation,Long> {
    Page<Rotation> findByNameContainingIgnoreCase(String search, Pageable pageable);
    
    // Check if a supervisor manages a specific intern via assignedSupervisor and interns relationship
    boolean existsByAssignedSupervisorIdAndInterns_Id(Long supervisorId, Long internId);
}
